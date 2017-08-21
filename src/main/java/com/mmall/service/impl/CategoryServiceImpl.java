package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by yao on 2017/8/18.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService{
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse<List<Category>> getCategory(int categoryId){
        if(categoryId != 0){
            int resultCount = categoryMapper.selectCheckCategoryExistById(categoryId);
            if(resultCount == 0){
                return ServerResponse.createByErrorMessage("该品类不存在");
            }
        }
        List<Category> categories = categoryMapper.listCategoryByParentId(categoryId);
        if(org.apache.commons.collections.CollectionUtils.isEmpty(categories)){
            logger.info("未找到当前品类的子品类");
        }
        return ServerResponse.createBySuccess(categories);
    }

    public ServerResponse<String> addCategory(int parentId,String categoryName){
        if(parentId != 0){
            int resultCount = categoryMapper.selectCheckCategoryExistById(parentId);
            if(resultCount == 0){
                return ServerResponse.createByErrorMessage("父品类不存在");
            }
        }
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true);
        int insertCount = categoryMapper.insertSelective(category);
        if(insertCount > 0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse<String> setCategoryName(int categoryId, String categoryName){
        int resultCount = categoryMapper.selectCheckCategoryExistById(categoryId);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("未找到该品类");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    public ServerResponse<List<Integer>> getDeepCategory(int categoryId){
        if(categoryId != 0){
            int resultCount = categoryMapper.selectCheckCategoryExistById(categoryId);
            if(resultCount == 0){
                return ServerResponse.createByErrorMessage("该品类不存在");
            }
        }
        Set<Category> categorySet = Sets.newHashSet();
        recursiveCategory(categorySet,categoryId);
        List<Integer> categoryIdList = Lists.newArrayList();
        for(Category temp : categorySet){
            categoryIdList.add(temp.getId());
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    /**
     * 找出当前品类所有字品类的递归方法
     * @param categorySet
     * @param categoryId
     * @return
     */
    private Set<Category> recursiveCategory(Set<Category> categorySet,int categoryId){
        if(categoryId != 0){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category != null){
                categorySet.add(category);
            }else{
                return categorySet;
            }
        }
        List<Category> categoryList = categoryMapper.listCategoryByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            return categorySet;
        }else{
            for(Category temp : categoryList){
                recursiveCategory(categorySet,temp.getId());
            }
        }
        return categorySet;
    }
}
