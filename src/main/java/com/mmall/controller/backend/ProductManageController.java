package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by yao on 2017/8/21.
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

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

    @RequestMapping(value = "upload.do")
    @ResponseBody
    public ServerResponse uploadFile(HttpSession session,MultipartFile upload_file, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录管理员");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("无权限操作");
        }
        String uploadFilePath = request.getServletContext().getRealPath("upload");
        String realFileName = iFileService.uploadFile(upload_file,uploadFilePath);
        if(realFileName == null){
            return ServerResponse.createByErrorMessage("上传文件失败");
        }
        Map<String,String> resultFileMap = Maps.newHashMap();
        resultFileMap.put("uri",realFileName);
        resultFileMap.put("url", PropertiesUtil.getParam("ftp.server.http.prefix") + realFileName);
        return ServerResponse.createBySuccess(resultFileMap);
    }

    @RequestMapping(value = "richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, MultipartFile upload_file, HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success","false");
            resultMap.put("msg","用户未登录，请登录管理员");
            return resultMap;
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            resultMap.put("success","false");
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
        String uploadFilePath = request.getServletContext().getRealPath("upload");
        String realFileName = iFileService.uploadFile(upload_file,uploadFilePath);
        if(realFileName == null){
            resultMap.put("success","false");
            resultMap.put("msg","上传文件失败");
            return resultMap;
        }
        resultMap.put("success","false");
        resultMap.put("msg","上传成功");
        resultMap.put("file_path",PropertiesUtil.getParam("ftp.server.http.prefix") + realFileName);
        response.setHeader("Access-Control-Allow-Headers","X-File-Name");
        return resultMap;
    }
}
