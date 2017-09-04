package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    List<Cart> listCartByUserId(Integer userId);

    int selectCartChecked(Integer userId);

    Cart selectByUserIdAndProductId(@Param("userId")Integer userId,@Param("productId")Integer productId);

    int deleteByUserIdAndProductIdSet(@Param("userId")Integer userId,@Param("productIdSet")Set<Integer> productIdSet);

    int updateProductCheckedStatus(@Param("userId")Integer userId,@Param("productId")Integer productId,@Param("checkedOrUnChecked")Integer checkedOrUnChecked);

    int selectCartProductCount(Integer userId);

    List<Cart> listCheckedCartByUserId(Integer userId);

    int deleteCheckedCartAfterOrderCreated(Integer userId);
}