package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by yao on 2017/8/16.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        int resultCount = userMapper.selectCheckUsernameExist(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        //MD5用户密码加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectByUsernameAndPassword(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("登录密码错误");
        }else{
            user.setPassword(null);
            user.setQuestion(null);
            user.setAnswer(null);
            return ServerResponse.createBySuccess(user);
        }
    }

    public ServerResponse<User> manageLogin(String username, String password){
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        User user = userMapper.selectByUsername(username);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("不是管理员用户，无法登陆");
        }
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        user = userMapper.selectByUsernameAndPassword(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("登录密码错误");
        }else{
            user.setPassword(null);
            user.setQuestion(null);
            user.setAnswer(null);
            return ServerResponse.createBySuccess(user);
        }
    }

    public ServerResponse<String> register(User user){
        ServerResponse validResponse = checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        //MD5密码加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        user.setRole(Const.Role.ROLE_CUSTOMER); //普通用户
        int registerCount = userMapper.insert(user);
        if(registerCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }else{
            return ServerResponse.createBySuccessMessage("注册成功");
        }
    }

    public ServerResponse<String> checkValid(String str,String type){
        if(org.apache.commons.lang3.StringUtils.isNotBlank(type)){
            /*---开始校验---*/
            if(Const.USERNAME.equals(type)){
                int usernameCount = userMapper.selectCheckUsernameExist(str);
                if(usernameCount > 0){
                    return ServerResponse.createByErrorMessage("用户已存在");
                }
            }else if(Const.EMAIL.equals(type)){
                int emailCount = userMapper.selectCheckEmailExist(str);
                if(emailCount > 0){
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        }else{
            ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse<String> forgetGetQuestion(String username){
        ServerResponse validResponse = checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){  //校验成功说明用户名不存在，返回错误信息
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectForgetQuestionByUsername(username);
        if(!org.apache.commons.lang3.StringUtils.isNotBlank(question)){
            return ServerResponse.createByErrorMessage("该用户未设置找回密码问题");
        }
        return ServerResponse.createBySuccess(question);
    }

    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
        if(StringUtils.isBlank(username) || question == null || answer == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        int checkCount = userMapper.selectForgetCheckAnswer(username,question,answer);
        if(checkCount == 0){
            return ServerResponse.createByErrorMessage("问题答案错误");
        }
        //说明问题以及问题的答案是这个用户的，并且是正确的
        String forgetToken = UUID.randomUUID().toString();
        TokenCache.setKeyAndValue(TokenCache.TOKEN_PREFIX + username,forgetToken);
        return ServerResponse.createBySuccess(forgetToken);
    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(StringUtils.isBlank(username) || StringUtils.isBlank(passwordNew) || forgetToken == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        ServerResponse validResponse = checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){  //校验成功说明用户名不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getValueByKey(TokenCache.TOKEN_PREFIX + username);
        if(!org.apache.commons.lang3.StringUtils.isNotBlank(token)){
            return ServerResponse.createByErrorMessage("token已经失效");
        }
        if(org.apache.commons.lang3.StringUtils.equals(token,forgetToken)){
            String md5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updateForgetResetPassword(username,md5PasswordNew);
            if(resultCount > 0){
                return  ServerResponse.createBySuccessMessage("修改密码成功");
            }else{
                return ServerResponse.createByErrorMessage("修改密码操作失效");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误，请用户重新获取修改密码的token");
        }
    }

    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        if(StringUtils.isBlank(passwordOld) || StringUtils.isBlank(passwordNew)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        int resultCount = userMapper.selectCheckPasswordOld(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码输入错误");
        }
        String md5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
        user.setPassword(md5PasswordNew);
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("修改密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<User> updateInformation(User user){
        //验证新的邮箱是否已被其他用户使用，如果新邮箱就是当前用户正在使用的邮箱是正确的
        int resultCount = userMapper.selectCheckEmailExistWithUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("邮箱已被使用");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount == 0){
            return ServerResponse.createByErrorMessage("更新个人信息失败");
        }
        User newUser = userMapper.selectByPrimaryKey(user.getId());
        newUser.setPassword(null);
        return ServerResponse.createBySuccess("更新个人信息成功",newUser);
    }

    public ServerResponse<User> getInformation(Integer id){
        if(id == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        User user = userMapper.selectByPrimaryKey(id);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(null);
        return ServerResponse.createBySuccess(user);
    }

}
