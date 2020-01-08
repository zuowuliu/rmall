package com.rmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    //用于在session中记录当前登录的用户的信息的key
    public static final String CURRENT_USER = "currentUser";

    //token的前缀
    public static final String TOKEN_PREFIX = "token_";

    //缓存设置的key的时间
    public interface RedisCacheExtime{
        int REDIS_SESSION_EXTIME = 60 * 30;//设置成30分钟
    }

    //用于在校验的时候使用
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    //利用接口来实现常量分组
    public interface IRole{
        int ROLE_CUSTOMER=0;//普通用户
        int ROLE_ADMIN=1;//管理员用户
    }

    //创建商品的价格顺序表
    public interface ProductPriceOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    //创建商品的状态信息(避免在数据库中能查到，但是商家已经不想卖了，将之下架处理了)
    public enum ProductStatusEnum{
        ON_SALE(1,"在线");
        private String value;
        private int code;
        ProductStatusEnum(int code,String value){
            this.code=code;
            this.value=value;
        }

        public String getValue(){
            return value;
        }

        public int getCode(){
            return code;
        }
    }
    //设置购物车的商品的是否选中的状态
    public interface Cart{
        int CHECKED = 1;//购物车中的商品被选中状态
        int UN_CHECKED = 0;
        String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";
        String LIMIT_NUM_FAIL="LIMIT_NUM_FAIL";
    }

    //声明支付状态的一些枚举(但并不属于是支付宝回调里面的内容)
    public enum OrderSatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");
        ;

        OrderSatusEnum(int code,String value){
            this.code=code;
            this.value=value;
        }
        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        //对声明支付状态的枚举类的进一步封装
        public static OrderSatusEnum codeOf(int code){
            for(OrderSatusEnum orderSatusEnum : values()){//values()枚举类的数组
                if(orderSatusEnum.getCode() == code){
                    return orderSatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    //处理支付宝回调的信息
    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";//等待支付
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";//交易成功

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    //用于以后扩展支付的方式的枚举
    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");

        PayPlatformEnum(int code,String value){
            this.code=code;
            this.value=value;
        }
        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    //设置支付的类型(在线支付)
    public enum PaymentTypeEnum{

        ONLINE_PAY(1,"在线支付");

        PaymentTypeEnum(int code,String value){
            this.code=code;
            this.value=value;
        }
        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        //对支付类型的枚举类的进一步封装
        public static PaymentTypeEnum codeOf(int code){
            for(PaymentTypeEnum paymentTypeEnum : values()){//values()枚举类的数组
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }


}
