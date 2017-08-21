package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yao on 2017/8/16.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)      //序列化的时候将值为null的字段去除
public class ServerResponse<T> implements Serializable{

    private int status;
    private String msg;
    private T data;

    private ServerResponse(int status){
        this.status = status;
    }

    private ServerResponse(int status,String msg){
        this.status = status;
        this.msg = msg;
    }

    private ServerResponse(int status,T data){
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status,String msg,T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus(){
        return this.status;
    }
    public String getMsg(){
        return this.msg;
    }
    public T getData(){
        return this.data;
    }

    public static <A> ServerResponse<A> createBySuccess(){
        return new ServerResponse<A>(ResponseCode.SUCCESS.getCode());
    }

    public static <A> ServerResponse<A> createBySuccessMessage(String msg){
        return new ServerResponse<A>(ResponseCode.SUCCESS.getCode(),msg);
    }

    public static <A> ServerResponse<A> createBySuccess(A data){
        return new ServerResponse<A>(ResponseCode.SUCCESS.getCode(),data);
    }

    public static <A> ServerResponse<A> createBySuccess(String msg,A data){
        return new ServerResponse<A>(ResponseCode.SUCCESS.getCode(),msg,data);
    }

    public static <A> ServerResponse<A> createByError(){
        return new ServerResponse<A>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public static <A> ServerResponse<A> createByErrorMessage(String errorMsg){
        return new ServerResponse<A>(ResponseCode.ERROR.getCode(),errorMsg);
    }

    public static <A> ServerResponse<A> createByErrorCodeMessage(int errorCode,String errorMsg){
        return new ServerResponse<A>(errorCode,errorMsg);
    }

}
