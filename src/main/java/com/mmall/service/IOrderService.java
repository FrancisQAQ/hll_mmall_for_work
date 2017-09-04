package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Created by yao on 2017/8/31.
 */
public interface IOrderService {

    @Transactional(rollbackFor = Exception.class)
    ServerResponse createOrder(Integer userId,Integer shippingId);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse<PageInfo> list(Integer userId,int pageSize,int pageNum);

    ServerResponse getOrderDetail(Integer userId,Long orderNo);

    ServerResponse manageGetOrder(Long orderNo);

    ServerResponse cancelOrder(Integer userId,Long orderNo);

    ServerResponse sendGoods(Long orderNo);

    ServerResponse pay(Integer userId,Long orderNo,String path);

    ServerResponse alipayCallback(Map<String,String> params);

    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);
}
