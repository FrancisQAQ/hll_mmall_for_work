package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVO;
import com.mmall.vo.ProductListVO;
import com.mmall.vo.ProductSearchVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yao on 2017/8/21.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService{

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse<String> manageUpdateOrSaveProduct(Product product){
        if(product != null){
            if(StringUtils.isNotBlank(product.getSubImages())){     //前端传过来的子图参数不为空
                String[] images = product.getSubImages().split(",");
                if(images.length > 0){
                    product.setMainImage(images[0]);
                }
            }else{
                return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
            }
            if(product.getId() == null){    //id为空，这是新增产品的操作
                int categoryIdCount = categoryMapper.selectCheckCategoryExistById(product.getCategoryId());
                if(categoryIdCount > 0){    //如果品类表中有此品类的ID时
                    int saveCount = productMapper.insert(product);
                    if(saveCount > 0){
                        return ServerResponse.createBySuccessMessage("新增产品成功");
                    }else{      //如果品类表中无此品类的ID时
                        return ServerResponse.createByErrorMessage("新增产品失败");
                    }
                }else{
                    return ServerResponse.createByErrorMessage("无此品类的产品");
                }
            }else{      //id不为空，这是更新产品信息的操作
                int updateCount = productMapper.updateByPrimaryKey(product);
                if(updateCount > 0){
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                }else{
                    return ServerResponse.createByErrorMessage("更新产品失败");
                }
            }
        }else{
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
    }

    public ServerResponse<String> manageSetSaleStatus(Integer productId,Integer status){
        int resultCount = productMapper.selectCheckProductExistById(productId);
        if(resultCount > 0){
            Product product = new Product();
            product.setId(productId);
            product.setStatus(status);
            resultCount = productMapper.updateByPrimaryKeySelective(product);
            if(resultCount > 0){
                return ServerResponse.createBySuccessMessage("修改产品状态成功");
            }else{
                return ServerResponse.createByErrorMessage("修改产品状态失败");
            }
        }else{
            return ServerResponse.createByErrorMessage("产品已下架或删除");
        }
    }


    public ServerResponse<ProductDetailVO> manageGetProductDetail(Integer productId){
        Product product = productMapper.selectByPrimaryKey(productId);
        if(productId != null){
            if(product != null){
                ProductDetailVO productDetailVO = assembleProductDetailVO(product);
                Integer parentId = categoryMapper.selectParentIdByPrimaryKey(productId);
                if(parentId != null){
                    productDetailVO.setParentCategoryId(parentId);
                }else{
                    productDetailVO.setParentCategoryId(0);
                }
                return ServerResponse.createBySuccess(productDetailVO);
            }else{
                return ServerResponse.createByErrorMessage("产品已下架或删除");
            }
        }else{
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
    }

    public ProductDetailVO assembleProductDetailVO(Product product){
        ProductDetailVO productDetailVO = new ProductDetailVO();
        productDetailVO.setId(product.getId());
        productDetailVO.setCategoryId(product.getCategoryId());
        productDetailVO.setName(product.getName());
        productDetailVO.setImageHost(PropertiesUtil.getDefaultParam("ftp.server.http.prefix","http://img.happymmall.com/"));
        productDetailVO.setSubtitle(product.getSubtitle());
        productDetailVO.setMainImage(product.getMainImage());
        productDetailVO.setSubImages(product.getSubImages());
        productDetailVO.setDetail(product.getDetail());
        productDetailVO.setPrice(product.getPrice());
        productDetailVO.setStock(product.getStock());
        productDetailVO.setStatus(product.getStatus());
        productDetailVO.setCreateTime(DateTimeUtil.formatDateByDefaultFormat(product.getCreateTime()));
        productDetailVO.setUpdateTime((DateTimeUtil.formatDateByDefaultFormat(product.getUpdateTime())));
        return productDetailVO;
    }

    public ServerResponse<PageInfo> manageListProduct(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.listProduct();
        List<ProductListVO> productListVOList = Lists.newArrayList();
        for(Product temp : productList){
            ProductListVO productListVO = assembleProductListVO(temp);
            productListVOList.add(productListVO);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ProductListVO assembleProductListVO(Product product){
        ProductListVO productListVO = new ProductListVO();
        productListVO.setId(product.getId());
        productListVO.setCategoryId(product.getCategoryId());
        productListVO.setName(product.getName());
        productListVO.setSubtitle(product.getSubtitle());
        productListVO.setMainImage(product.getMainImage());
        productListVO.setPrice(product.getPrice());
        productListVO.setStatus(product.getStatus());
        return productListVO;
    }

    public ServerResponse<PageInfo> manageSearchProduct(int pageNum, int pageSize,String productName,Integer productId){
        if(StringUtils.isBlank(productName) && productId == null){  //兩個參數不能同時為空
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.listProductByProductNameAndPrimaryKey(productName,productId);
        List<ProductSearchVO> productSearchVOList = Lists.newArrayList();
        for(Product temp : productList){
            ProductSearchVO productSearchVO = assembleProductSearchVO(temp);
            productSearchVOList.add(productSearchVO);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productSearchVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ProductSearchVO assembleProductSearchVO(Product product){
        ProductSearchVO productSearchVO = new ProductSearchVO();
        productSearchVO.setId(product.getId());
        productSearchVO.setCategoryId(product.getCategoryId());
        productSearchVO.setName(product.getName());
        productSearchVO.setSubtitle(product.getSubtitle());
        productSearchVO.setMainImage(product.getMainImage());
        productSearchVO.setPrice(product.getPrice());
        return productSearchVO;
    }

    public ServerResponse<PageInfo> listProduct(Integer categoryId,String keyword,int pageNum,int pageSize,String orderBy){
        if(categoryId == null && StringUtils.isBlank(keyword)){ //如果cateId和keyword同时为空，则返回参数错误
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIds = Lists.newArrayList();
        if(categoryId != null){
            categoryIds = iCategoryService.getDeepCategory(categoryId).getData();
        }
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderStrs = orderBy.split("_");
                String orderStr = orderStrs[0] + " " + orderStrs[1];
                PageHelper.orderBy(orderStr);
            }
        }
        List<Product> productList = productMapper.listProductByCategoryIdsAndKeyword(categoryIds,keyword);
        PageInfo pageInfo = new PageInfo(productList);
        List<ProductListVO> productListVOList = Lists.newArrayList();
        for(Product temp : productList){
            ProductListVO productListVO = assembleProductListVO(temp);
            productListVOList.add(productListVO);
        }
        pageInfo.setList(productListVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<ProductDetailVO> getProductDetail(Integer productId){
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("该商品已下架或删除");
        }
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("该商品已下架或删除");
        }
        ProductDetailVO productDetailVO = assembleProductDetailVO(product);
        return ServerResponse.createBySuccess(productDetailVO);
    }
}
