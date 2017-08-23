package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by yao on 2017/8/21.
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    @RequestMapping(value = "save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> updateOrSaveProduct(HttpSession session,Product product){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return iProductService.manageUpdateOrSaveProduct(product);
    }

    @RequestMapping(value = "set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session,Integer productId,Integer status){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return iProductService.manageSetSaleStatus(productId,status);
    }

    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVO> getProductDetail(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return iProductService.manageGetProductDetail(productId);
    }

    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> listProduct(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return iProductService.manageListProduct(pageNum,pageSize);
    }

    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> searchProduct(HttpSession session
            , @RequestParam(value = "pageNum",defaultValue = "1") int pageNum
            , @RequestParam(value = "pageSize",defaultValue = "10") int pageSize
            ,@RequestParam(value = "productName",required = false) String productName
            ,@RequestParam(value = "productId",required = false) Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        return iProductService.manageSearchProduct(pageNum,pageSize,productName,productId);
    }
}
