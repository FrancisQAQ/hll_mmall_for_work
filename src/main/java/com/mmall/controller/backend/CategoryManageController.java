package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by yao on 2017/8/18.
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {
    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 获取当前商品分类的子商品分类
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Category>> getCategory(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0")Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("当前用户不是管理员用户，无权限操作");
        }
        return iCategoryService.getCategory(categoryId);
    }

    /**
     * 增加新的商品分类
     * @param session
     * @param parentId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session, @RequestParam(value = "parentId",defaultValue = "0")Integer parentId,String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("当前用户不是管理员用户，无权限操作");
        }
        return iCategoryService.addCategory(parentId,categoryName);
    }

    /**
     * 更新商品种类的名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session, Integer categoryId, String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("当前用户不是管理员用户，无权限操作");
        }
        return iCategoryService.setCategoryName(categoryId,categoryName);
    }

    /**
     * 获取当前商品分类的所有的递归子分类
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getDeepCategory(HttpSession session,Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("当前用户不是管理员用户，无权限操作");
        }
        return iCategoryService.getDeepCategory(categoryId);
    }
}
