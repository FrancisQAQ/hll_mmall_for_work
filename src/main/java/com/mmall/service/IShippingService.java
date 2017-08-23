package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

import java.util.List;

/**
 * Created by yao on 2017/8/23.
 */
public interface IShippingService {

    ServerResponse addShippingAddress(Shipping shipping);

    ServerResponse deleteShippingAddress(Integer userId,Integer shippingId);

    ServerResponse updateShippingAddress(Shipping shipping);

    ServerResponse selectSingleShippingAddress(Integer userId,Integer shippingId);

    ServerResponse<List<Shipping>> listShippingAddress(Integer userId);
}
