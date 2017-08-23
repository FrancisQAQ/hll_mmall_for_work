package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by yao on 2017/8/22.
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVO> getCart(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.getCart(user.getId());
    }

    @RequestMapping(value = "add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVO> addProduct(HttpSession session,Integer productId,Integer count){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.addProduct(user.getId(),productId,count);
    }

    @RequestMapping(value = "update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVO> updateProductQuantity(HttpSession session,Integer productId,Integer count){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.updateProductQuantity(user.getId(),productId,count);
    }

    @RequestMapping(value = "delete_product.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVO> deleteProduct(HttpSession session,String productIds){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    @RequestMapping(value = "select.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVO> selectSimpleProductChecked(HttpSession session,Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.updateProductCheckedStatus(user.getId(),productId,Const.Cart.CHECKED);
    }

    @RequestMapping(value = "un_select.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVO> selectSimpleProductUnChecked(HttpSession session,Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        return iCartService.updateProductCheckedStatus(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    @RequestMapping(value = "select_all.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVO> selectAllProductChecked(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.updateProductCheckedStatus(user.getId(),null,Const.Cart.CHECKED);
    }

    @RequestMapping(value = "un_select_all.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVO> selectAllProductUnChecked(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.updateProductCheckedStatus(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    @RequestMapping(value = "get_cart_product_count.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }
}
