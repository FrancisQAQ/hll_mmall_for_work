package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * Created by yao on 2017/8/18.
 */
public interface ICategoryService {
    ServerResponse<List<Category>> getCategory(Integer categoryId);

    ServerResponse<String> addCategory(Integer parentId,String categoryName);

    ServerResponse<String> setCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Integer>> getDeepCategory(Integer categoryId);
}
