package com.rmall.common;

/**
 * @author 大神爱吃茶
 * */

public enum ResponseCode{

    //设置的几种状态即对应的状态码

    SUCCESS("SUCCESS",0),
    ERROR("ERROR",1),
    NEED_LOGIN("NEED_LOGIN",10),
    ILLEGAL_ARGUMENT("ILLEGAL_ARGUMENT",2);

    //设置的两个私有的final属性来对应上面枚举类的对应的两个位置

    private final String condition;
    private final int code;

    ResponseCode(String condition, int code) {
        this.condition = condition;
        this.code = code;
    }

    public String getCondition() {
        return condition;
    }

    public int getCode() {
        return code;
    }
}