package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteShippingAddress(@Param("userId")Integer userId,@Param("shippingId") Integer shippingId);

    int updateShippingAddress(Shipping shipping);

    Shipping selectSingleShippingAddress(@Param("userId")Integer userId,@Param("shippingId") Integer shippingId);

    List<Shipping> listShippingAddressByUserId(Integer userId);
}