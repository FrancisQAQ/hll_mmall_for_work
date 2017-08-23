package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVO;

/**
 * Created by yao on 2017/8/22.
 */
public interface ICartService {
    ServerResponse<CartVO> getCart(Integer userId);

    ServerResponse<CartVO> addProduct(Integer userId,Integer productId,Integer count);

    ServerResponse<CartVO> updateProductQuantity(Integer userId,Integer productId,Integer count);

    ServerResponse<CartVO> deleteProduct(Integer userId,String productIds);

    ServerResponse<CartVO> updateProductCheckedStatus(Integer userId,Integer productId,Integer checkedOrUnChecked);

    ServerResponse<Integer> getCartProductCount(Integer userId);
}
