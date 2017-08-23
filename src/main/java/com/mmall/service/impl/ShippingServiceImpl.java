package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yao on 2017/8/23.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService{
    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse addShippingAddress(Shipping shipping){
        int insertCount = shippingMapper.insert(shipping);
        return insertCount > 0 ? ServerResponse.createBySuccess("新建地址成功",shipping.getId()) : ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse deleteShippingAddress(Integer userId,Integer shippingId){
        int deleteCount = shippingMapper.deleteShippingAddress(userId,shippingId);
        return deleteCount > 0 ? ServerResponse.createBySuccess("删除地址成功") : ServerResponse.createByErrorMessage("删除地址失败");
    }

    public ServerResponse updateShippingAddress(Shipping shipping){
        int updateCount = shippingMapper.updateShippingAddress(shipping);
        return updateCount > 0 ? ServerResponse.createBySuccess("更新地址成功") : ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse selectSingleShippingAddress(Integer userId,Integer shippingId){
        Shipping shipping = shippingMapper.selectSingleShippingAddress(userId,shippingId);
        return shipping != null ? ServerResponse.createBySuccess(shipping) : ServerResponse.createByErrorMessage("未找到该地址信息");
    }

    public ServerResponse<List<Shipping>> listShippingAddress(Integer userId){
        List<Shipping> shippingList = shippingMapper.listShippingAddressByUserId(userId);
        return ServerResponse.createBySuccess(shippingList);
    }
}
