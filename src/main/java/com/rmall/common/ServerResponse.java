package com.rmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
/**
 * @author 大神爱吃茶
 *
 * */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//过滤掉为空值的序列化的值
public class ServerResponse<T> implements Serializable {
    private int statusCode;
    private String msg;
    private T data;

    //（私有构造器）保证每一个构造器的传参都包含的有int statusCode
    private ServerResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    private ServerResponse(int statusCode, String msg) {
        this.statusCode = statusCode;
        this.msg = msg;
    }

    private ServerResponse(int statusCode, T data) {
        this.statusCode = statusCode;
        this.data = data;
    }

    private ServerResponse(int statusCode, String msg, T data) {
        this.statusCode = statusCode;
        this.msg = msg;
        this.data = data;
    }

    //开放响应类的这三个属性
    public int getStatusCode() {
        return statusCode;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    //通过成功的信息来创建响应类对象的方法
    public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    public static <T> ServerResponse<T> createBySuccess(String successMessage){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), successMessage);
    }
    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    public static <T> ServerResponse<T> createBySuccess(String successMessage,T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), successMessage,data);
    }

    //通过错误的信息来创建响应类对象的方法
    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode());
    }
    public static <T> ServerResponse<T> createByError(String errorMessage){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), errorMessage);
    }
    public static <T> ServerResponse<T> createByError(int errorCode,String errorMessage){
        return new ServerResponse<T>(errorCode, errorMessage);
    }
    @JsonIgnore
    //在返回JSON的数据中忽略掉isSuccess()这个方法
    public boolean isSuccess(){
        return this.statusCode==ResponseCode.SUCCESS.getCode();
    }
}
