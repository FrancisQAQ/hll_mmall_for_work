package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVO;
import com.mmall.vo.OrderVO;
import com.mmall.vo.ShippingVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by yao on 2017/8/31.
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService{

    private static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static AlipayTradeService tradeService;
    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

    }

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;


    /**
     * 创建新的订单业务处理方法
     * @param userId
     * @param shippingId
     * @return
     */
    @Transactional(rollbackFor = Throwable.class,propagation=Propagation.REQUIRES_NEW)
    public ServerResponse createOrder(Integer userId,Integer shippingId){
        Shipping shipping = shippingMapper.selectSingleShippingAddress(userId,shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("当前用户不存在此收货地址");
        }
        Order order = new Order();
        Long orderNo = System.currentTimeMillis() + new Random().nextInt(1000)%10;  //生成订单号
        BigDecimal totalPrice;    //购物车中商品的总价
        List<Cart> cartList = cartMapper.listCheckedCartByUserId(userId);   //获取当前用户购物车中已勾选的商品
        ServerResponse serverResponse = getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        for(OrderItem orderItemItem : orderItemList){
            orderItemItem.setOrderNo(orderNo);
        }
        //接着检查orderItemList判断购物车是否为空
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        totalPrice = generateTotalPrice(orderItemList);
        /*----构建订单---------*/
        order.setOrderNo(orderNo);  //订单号
        order.setUserId(userId);    //用户ID
        order.setShippingId(shippingId);    //收货地址ID
        order.setPayment(totalPrice);   //订单商品总价
        order.setPostage(0);    //运费为0元
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());   //支付类型是在线支付
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());    //订单是未支付的状态
        /*--------------------*/
            int insertOrderCount = orderMapper.insertSelective(order); //将订单信息存入数据库中
            if(insertOrderCount == 0){
                return ServerResponse.createByErrorMessage("生成订单失败");
            }
            orderItemMapper.batchInsert(orderItemList);

        //接着减少对应商品的库存，以及清空购物车
        //减去库存总数
        reduceProductStock(orderItemList);
        //清空购物车
        cartMapper.deleteCheckedCartAfterOrderCreated(userId);

        //接下来组装需要返回给前端的该订单数据
        Order newOrder = orderMapper.selectByOrderNoAndUserId(userId,orderNo);  //将之前插入到数据库的订单数据重新获取出来，此时的数据带有了creaTime
        List<OrderItem> newOrderItemList = orderItemMapper.listOrderItemByUserIdAndOrderNo(userId,orderNo);    ////将之前插入到数据库的订单详情数据重新获取出来，此时的数据带有了creaTime
        OrderVO orderVO = assembleOrderVO(newOrder,newOrderItemList);
        return ServerResponse.createBySuccess(orderVO);
    }

    /**
     * 通过遍历累加orderItemList的所有元素得到订单商品的总价
     * @param orderItemList
     * @return
     */
    public BigDecimal generateTotalPrice(List<OrderItem> orderItemList){
        BigDecimal totalPrice = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return totalPrice;
    }
    /**
     * 通过cartList以及userId生成需要的orderItemList
     * @param userId
     * @param cartList
     * @return
     */
    public ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList = Lists.newArrayList();
        //首先检查cartList判断购物车是否为空
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //遍历每一个cart计算出商品的总价格
        for(Cart cartItem : cartList){
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if(product == null){
                return ServerResponse.createByErrorMessage("购物车中有商品已经下架或删除，请重新提交订单");
            }
            if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
                return ServerResponse.createByErrorMessage("商品：" + product.getName() + "已下架或者删除");
            }
            if(cartItem.getQuantity() >  product.getStock()){   //检查库存是否充足
                return ServerResponse.createByErrorMessage("商品：" + product.getName() + "库存不足");
            }
            OrderItem orderItem = new OrderItem();
            /*----构建订单明细---------*/
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());  //商品当前的单价
            orderItem.setQuantity(cartItem.getQuantity());
            BigDecimal productTotalPrice = BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity().doubleValue());   //计算当前商品的总价
            orderItem.setTotalPrice(productTotalPrice);
            /*--------------------------*/
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 返回组装好的OrderVO
     * @param order
     * @param orderItemList
     * @return
     */
    public OrderVO assembleOrderVO(Order order,List<OrderItem> orderItemList){
        if(order == null){
            return null;
        }
        OrderVO orderVO = new OrderVO();
        /*--------组装orderVO-----------*/
        orderVO.setOrderNo(order.getOrderNo());
        orderVO.setShippingId(order.getShippingId());
        orderVO.setPayment(order.getPayment());
        orderVO.setPaymentType(order.getPaymentType());
        orderVO.setPaymentTypeDesc(Const.PaymentTypeEnum.valueOfPaymentType(orderVO.getPaymentType())); //支付方式的文字描述
        orderVO.setPostage(order.getPostage());
        orderVO.setStatus(order.getStatus());
        orderVO.setStatusDesc(Const.OrderStatusEnum.descOfOrderStatus(orderVO.getStatus()));    //订单状态的文字描述
        orderVO.setPaymentTime(DateTimeUtil.formatDateByDefaultFormat(order.getPaymentTime()));
        orderVO.setSendTime(DateTimeUtil.formatDateByDefaultFormat(order.getSendTime()));
        orderVO.setEndTime(DateTimeUtil.formatDateByDefaultFormat(order.getEndTime()));
        orderVO.setCloseTime(DateTimeUtil.formatDateByDefaultFormat(order.getCloseTime()));
        orderVO.setCreateTime(DateTimeUtil.formatDateByDefaultFormat(order.getCreateTime()));
        orderVO.setImageHost(PropertiesUtil.getParam("ftp.server.http.prefix"));
        /*-------------------------------*/
        List<OrderItemVO> orderItemVOList = assembleOrderItemVOList(orderItemList);
        orderVO.setOrderItemVOList(orderItemVOList);
        Shipping shipping = shippingMapper.selectSingleShippingAddress(order.getUserId(),order.getShippingId());
        if(shipping != null){
            orderVO.setReceiverName(shipping.getReceiverName());    //收件人姓名
        }
        return orderVO;
    }

    /**
     * 返回组装好的List<OrderItemVO>
     * @param orderItemList
     * @return
     */
    public List<OrderItemVO> assembleOrderItemVOList(List<OrderItem> orderItemList){
        if(orderItemList == null){
            return null;
        }
        List<OrderItemVO> orderItemVOList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            OrderItemVO orderItemVO = new OrderItemVO();
            /*--------------组装orderItemVO-------------*/
            orderItemVO.setOrderNo(orderItem.getOrderNo());
            orderItemVO.setProductId(orderItem.getProductId());
            orderItemVO.setProductName(orderItem.getProductName());
            orderItemVO.setProductImage(orderItem.getProductImage());
            orderItemVO.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVO.setQuantity(orderItem.getQuantity());
            orderItemVO.setTotalPrice(orderItem.getTotalPrice());
            orderItemVO.setCreateTime(DateTimeUtil.formatDateByDefaultFormat(orderItem.getCreateTime()));
            /*---------------------------------------*/
            orderItemVOList.add(orderItemVO);
        }
        return orderItemVOList;
    }

    /**
     * 减去对应商品的库存总数
     * @param orderItemList
     */
    public void reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            Product updateProduct = new Product();
            updateProduct.setId(product.getId());
            updateProduct.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(updateProduct);
        }
    }

    /**
     * 将购物车中已勾选的商品生成预览订单
     * @param userId
     * @return
     */
    public ServerResponse getOrderCartProduct(Integer userId){
        Map<String,Object> resultMap = Maps.newHashMap();
        List<Cart> cartList = cartMapper.listCheckedCartByUserId(userId);
        ServerResponse serverResponse = getCartOrderItem(userId,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        List<OrderItemVO> orderItemVOList = assembleOrderItemVOList(orderItemList);
        BigDecimal totalPrice = generateTotalPrice(orderItemList);
        resultMap.put("orderItemVoList",orderItemVOList);
        resultMap.put("imageHost",PropertiesUtil.getParam("ftp.server.http.prefix"));
        resultMap.put("productTotalPrice",totalPrice);
        return ServerResponse.createBySuccess(resultMap);
    }

    public ServerResponse<PageInfo> list(Integer userId,int pageSize,int pageNum){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.listOrderByUserId(userId);
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVO> orderVOList = Lists.newArrayList();
        for(Order orderItem : orderList){
            List<OrderItem> orderItemList = orderItemMapper.listOrderItemByUserIdAndOrderNo(userId,orderItem.getOrderNo());
            OrderVO orderVO = assembleOrderVO(orderItem,orderItemList);
            orderVOList.add(orderVO);
        }
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse getOrderDetail(Integer userId,Long orderNo){
        Order order = orderMapper.selectByOrderNoAndUserId(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("没有找到订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.listOrderItemByUserIdAndOrderNo(userId,orderNo);
        OrderVO orderVO = assembleOrderVO(order,orderItemList);
        Shipping shipping = shippingMapper.selectSingleShippingAddress(userId,orderVO.getShippingId());
        ShippingVO shippingVO = null;
        if(shipping != null){
            shippingVO = assembleShippingVO(shipping);
        }
        orderVO.setShippingVO(shippingVO);
        return ServerResponse.createBySuccess(orderVO);
    }

    public ServerResponse manageGetOrder(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("没有找到订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.listOrderItemByUserIdAndOrderNo(order.getUserId(),orderNo);
        OrderVO orderVO = assembleOrderVO(order,orderItemList);
        Shipping shipping = shippingMapper.selectSingleShippingAddress(order.getUserId(),orderVO.getShippingId());
        ShippingVO shippingVO = null;
        if(shipping != null){
            shippingVO = assembleShippingVO(shipping);
        }
        orderVO.setShippingVO(shippingVO);
        return ServerResponse.createBySuccess(orderVO);
    }

    public ServerResponse cancelOrder(Integer userId,Long orderNo){
        Order order = orderMapper.selectByOrderNoAndUserId(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("没有找到订单");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createByErrorMessage("此订单已付款，无法被取消");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(0);
        int updateCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(updateCount == 0){
            return ServerResponse.createByErrorMessage("订单：" + order.getOrderNo() + "取消失败");
        }
        //订单取消成功后将订单中各个商品的库存还原
        List<OrderItem> orderItemList = orderItemMapper.listOrderItemByUserIdAndOrderNo(userId,orderNo);
        for(OrderItem orderItemItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItemItem.getProductId());
            Product updateProduct = new Product();
            updateProduct.setId(product.getId());
            updateProduct.setStock(product.getStock() + orderItemItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(updateProduct);
        }
        return ServerResponse.createBySuccessMessage("订单：" + order.getOrderNo() + "取消成功");
    }

    public ServerResponse sendGoods(Long orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("没有找到订单");
        }
        int updateCount = orderMapper.updateSendGoodsTime(orderNo,Const.OrderStatusEnum.SHIPPED.getCode());
        if(updateCount == 0){
            return ServerResponse.createByErrorMessage("发货失败");
        }
        return ServerResponse.createBySuccessMessage("发货成功");
    }

    public ShippingVO assembleShippingVO(Shipping shipping){
        ShippingVO shippingVO = new ShippingVO();
        shippingVO.setReceiverName(shipping.getReceiverName());
        shippingVO.setReceiverPhone(shipping.getReceiverPhone());
        shippingVO.setReceiverMobile(shipping.getReceiverMobile());
        shippingVO.setReceiverProvince(shipping.getReceiverProvince());
        shippingVO.setReceiverCity(shipping.getReceiverCity());
        shippingVO.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVO.setReceiverAddress(shipping.getReceiverAddress());
        shippingVO.setReceiverZip(shipping.getReceiverZip());
        return shippingVO;
    }


    /***
     * 支付功能业务处理方法
     * @param userId
     * @param orderNo
     * @param path
     * @return
     */
    public ServerResponse pay(Integer userId, Long orderNo,String path){
        return tradePrecreate(userId,orderNo,path);
    }

    private ServerResponse tradePrecreate(Integer userId, Long orderNo,String path){
        Order order = orderMapper.selectByOrderNoAndUserId(userId,orderNo);
        if(order == null){
            return ServerResponse.createBySuccessMessage("当前用户没有该订单");
        }
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("LOL道具城官网当面付扫码消费，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单：").append(outTradeNo).append("总金额为：").append(totalAmount).toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.listOrderItemByUserIdAndOrderNo(userId,orderNo);
        for(OrderItem orderItemItem : orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItemItem.getProductId().toString(), orderItemItem.getProductName(),
                    BigDecimalUtil.mul(orderItemItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(), orderItemItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getParam("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File tomcatPath = new File(path);
                if(!tomcatPath.exists()){
                    tomcatPath.setWritable(true);
                    tomcatPath.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String fileName = String.format("qr-%s.png", response.getOutTradeNo());
                String totalFiletPath = new StringBuilder().append(path).append("/").append(fileName).toString();
                File targetFile = new File(path,fileName);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, totalFiletPath);
                try {
                    FTPUtil.uploadFile(Lists.<File>newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("生成二维码图片异常",e);
                    return ServerResponse.createByErrorMessage("生成二维码图片异常");
                }
                Map<String,String> resultMap = Maps.newHashMap();
                resultMap.put("orderNo",order.getOrderNo().toString());
                resultMap.put("qrPath",new StringBuilder().append(PropertiesUtil.getParam("ftp.server.http.prefix")).append(targetFile.getName()).toString());
                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    /***
     * 支付宝当面付回调业务处理方法
     * @param params
     * @return
     */
    public ServerResponse alipayCallback(Map<String,String> params){
        Long orderNo = Long.parseLong(params.get("out_trade_no"));  //获取支付宝回调请求中的订单号
        String tradeNo = params.get("trade_no"); //获取支付宝回调请求中的支付宝交易凭证号
        String tradeStatus = params.get("trade_status");    //获取支付宝回调请求中的交易目前所处的状态
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("非LOL道具城订单，回调忽略");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccessMessage("支付宝重复调用");
        }
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            try {
                updateOrder.setPaymentTime(DateTimeUtil.strToDateByDefaultFormat(params.get("gmt_payment")));
                updateOrder.setStatus(Const.OrderStatusEnum.PAID.getCode());
            } catch (ParseException e) {
                logger.error("转换用户支付时间为Date异常",e);
            }
            orderMapper.updateByPrimaryKeySelective(updateOrder);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    /***
     * 查看订单支付状态业务处理方法
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.selectByOrderNoAndUserId(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("该用户并没有该订单,查询无效");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createByError();
    }
}
