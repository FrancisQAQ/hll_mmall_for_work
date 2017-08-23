package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by yao on 2017/8/16.
 */
public class Const {
    public static final String CURRENT_USER = "current_user";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public interface Role{
        int ROLE_CUSTOMER = 0;  //普通用户
        int ROLE_ADMIN = 1; //管理员
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Cart{
        int CHECKED = 1;    //勾选状态
        int UN_CHECKED = 0; //未勾选状态

        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
        String LIMIT_NUM_FAIL  = "LIMIT_NUM_FAIL";
    }
    public enum ProductStatusEnum{
        ON_SALE(1,"在线");
        private int code;
        private String desc;
        ProductStatusEnum(int code,String desc){
            this.code = code;
            this.desc = desc;
        }
        public int getCode(){
            return this.code;
        }
        public String getDesc(){
            return this.desc;
        }
    }
}
