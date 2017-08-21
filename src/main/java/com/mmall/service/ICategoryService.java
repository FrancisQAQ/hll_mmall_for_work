package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * Created by yao on 2017/8/18.
 */
public interface ICategoryService {
    ServerResponse<List<Category>> getCategory(int categoryId);

    ServerResponse<String> addCategory(int parentId,String categoryName);

    ServerResponse<String> setCategoryName(int categoryId, String categoryName);

    ServerResponse<List<Integer>> getDeepCategory(int categoryId);
}
