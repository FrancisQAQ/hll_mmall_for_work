package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by yao on 2017/9/1.
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> manageList(HttpSession session,
                                               @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                               @RequestParam(value = "pageNum",defaultValue = "1")int pageNum){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作，请登录管理员");
        }
        return iOrderService.list(null,pageSize,pageNum);
    }


    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse searchOrder(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作，请登录管理员");
        }
        return iOrderService.manageGetOrder(orderNo);
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作，请登录管理员");
        }
        return iOrderService.manageGetOrder(orderNo);
    }

    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse sendGoods(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作，请登录管理员");
        }
        return iOrderService.sendGoods(orderNo);
    }
}
