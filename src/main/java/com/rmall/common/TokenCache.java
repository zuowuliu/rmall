package com.rmall.common;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author 大神爱吃茶
 * */
public class TokenCache {

    //创建日志
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    //token的前缀
    public static final String TOKEN_PREFIX = "token_";

    //设置本地缓存，其中expireAfterAccess是有效期
    //初始化的值是10000，如果超过10000的话就会使用LRU算法
    //声明了localCache的key和value值都是String类型的
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().
            initialCapacity(1000).
            maximumSize(10000).
            expireAfterAccess(12, TimeUnit.HOURS).
            build(new CacheLoader<String, String>() {
                //下面这个方法是默认的数据加载实现，当调用get取值的时候，如果key没有对应的值，就调用这个方法来进行加载
                //即如果get没有拿到数据，就用load来加载缓存
                @Override
                public String load(String s) throws Exception {
                    //在这里可以初始化加载数据的缓存信息，读取数据库中信息或者是加载文件中的某些数据信息
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        localCache.put(key, value);
    }

    public static String getKey(String key){
        String value = null;
        try{
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error", e);
        }
        return null;
    }
}
