package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int selectCheckUsernameExist(String username);

    int selectCheckEmailExist(String email);

    User selectByUsernameAndPassword(@Param("username") String username,@Param("password") String password);

    User selectByUsername(@Param("username") String username);

    String selectForgetQuestionByUsername(String username);

    int selectForgetCheckAnswer(@Param("username")String username,@Param("question")String question,@Param("answer")String answer);

    int updateForgetResetPassword(@Param("username")String username,@Param("passwordNew")String passwordNew);

    int selectCheckPasswordOld(@Param("passwordOld") String passwordOld,@Param("id")Integer id);

    int selectCheckEmailExistWithUserId(@Param("email")String email,@Param("id")Integer id);
}