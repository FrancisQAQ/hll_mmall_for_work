package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.zxing.client.result.ProductParsedResult;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVO;
import com.mmall.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Created by yao on 2017/8/22.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService{

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVO> getCart(Integer userId){
        return generateCartVOList(userId);
    }

    private ServerResponse<CartVO> generateCartVOList(Integer userId){
        List<Cart> cartList = cartMapper.listCartByUserId(userId);
        List<CartProductVO> cartProductVOList = Lists.newArrayList();
        BigDecimal totalPrice = new BigDecimal(0);
        for(Cart cartTemp : cartList){
            Product product = productMapper.selectByPrimaryKey(cartTemp.getProductId());
            if(product != null){
                CartProductVO cartProductVO = new CartProductVO();
                cartProductVO.setId(cartTemp.getId());
                cartProductVO.setUserId(cartTemp.getUserId());
                cartProductVO.setProductId(cartTemp.getProductId());
                cartProductVO.setProductName(product.getName());
                cartProductVO.setProductSubtitle(product.getSubtitle());
                cartProductVO.setProductMainImage(product.getMainImage());
                cartProductVO.setProductPrice(product.getPrice());
                cartProductVO.setProductStatus(product.getStatus());
                cartProductVO.setProductStock(product.getStock());
                cartProductVO.setProductChecked(cartTemp.getChecked());
                Integer actualQuantity;
                if(product.getStock() >= cartTemp.getQuantity() ){
                    actualQuantity = cartTemp.getQuantity();
                    cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                }else{
                    actualQuantity = product.getStock();
                    cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                }
                cartProductVO.setQuantity(actualQuantity);
                cartProductVO.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVO.getQuantity()));
                if(cartTemp.getChecked() == Const.Cart.CHECKED){    //商品是勾选状态时才会将其总价添加到购物车总价中
                    totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),cartProductVO.getProductTotalPrice().doubleValue());
                }
                cartProductVOList.add(cartProductVO);
            }
        }
        CartVO cartVO = new CartVO();
        cartVO.setCartProductVOList(cartProductVOList);
        cartVO.setAllChecked(judgeAllChecked(userId));
        cartVO.setCartTotalPrice(totalPrice);
        cartVO.setImgHost(PropertiesUtil.getDefaultParam("ftp.server.http.prefix","http://img.happymmall.com/"));
        return ServerResponse.createBySuccess(cartVO);
    }

    private boolean judgeAllChecked(Integer userId){
        return cartMapper.selectCartChecked(userId) == 0 ? true : false;
    }


    public ServerResponse<CartVO> addProduct(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product != null){
            Cart cart = cartMapper.selectByUserIdAndProductId(userId,productId);
            Cart newCart = new Cart();
            if(cart == null){   //新增一条cart记录
                newCart.setUserId(userId);
                newCart.setChecked(Const.Cart.CHECKED); //商品添加到购物车中默认是勾选状态
                newCart.setProductId(productId);
                newCart.setQuantity(count);
                cartMapper.insert(newCart);
            }else{
                newCart.setId(cart.getId());
                newCart.setQuantity(cart.getQuantity() + count);
                cartMapper.updateByPrimaryKeySelective(newCart);
            }
        }
        return generateCartVOList(userId);
    }

    public ServerResponse<CartVO> updateProductQuantity(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId,productId);
        Cart newCart = new Cart();
        if(cart != null){
            newCart.setId(cart.getId());
            newCart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(newCart);
        }
        return generateCartVOList(userId);
    }

    public ServerResponse<CartVO> deleteProduct(Integer userId,String productIds){
        if(productIds == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        String[] productIdStrs = productIds.split(",");
        Set<Integer> productIdSet = Sets.newHashSet();
        for(String temp : productIdStrs){
            productIdSet.add(Integer.parseInt(temp));
        }
        cartMapper.deleteByUserIdAndProductIdSet(userId,productIdSet);
        return generateCartVOList(userId);
    }

    public ServerResponse<CartVO> updateProductCheckedStatus(Integer userId,Integer productId,Integer checkedOrUnChecked){
        if(checkedOrUnChecked == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.updateProductCheckedStatus(userId,productId,checkedOrUnChecked);
        return generateCartVOList(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }
}
