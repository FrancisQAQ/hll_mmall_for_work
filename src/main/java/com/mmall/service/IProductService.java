package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVO;

/**
 * Created by yao on 2017/8/21.
 */
public interface IProductService {
    ServerResponse<String> manageUpdateOrSaveProduct( Product product);

    ServerResponse<String> manageSetSaleStatus( Integer productId,Integer status);

    ServerResponse<ProductDetailVO> manageGetProductDetail(Integer productId);

    ServerResponse<PageInfo> manageListProduct(int pageNum, int pageSize);

    ServerResponse<PageInfo> manageSearchProduct(int pageNum, int pageSize,String productName,Integer productId);

    ServerResponse<PageInfo> listProduct(Integer categoryId,String keyword,int pageNum,int pageSize,String orderBy);

    ServerResponse<ProductDetailVO> getProductDetail(Integer productId);
}
