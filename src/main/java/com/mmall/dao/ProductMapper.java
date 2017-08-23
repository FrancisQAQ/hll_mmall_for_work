package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    int selectCheckProductExistById(Integer id);

    List<Product> listProduct();

    List<Product> listProductByProductNameAndPrimaryKey(@Param("productName") String productName, @Param("id") Integer id);

    List<Product> listProductByCategoryIdsAndKeyword(@Param("categoryIds")List<Integer> categoryIds,@Param("keyword")String keyword);

}