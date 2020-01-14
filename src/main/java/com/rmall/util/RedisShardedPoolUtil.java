package com.rmall.util;

import com.rmall.common.RedisShardedPool;
import com.rmall.common.RedisShardedPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

/**
 * @author 大神爱吃茶
 * @Date 2020/1/4 0004 下午 21:04
 */
public class RedisShardedPoolUtil {
    /**
     * 设置RedisShardedPoolUtil的目的就是将jedis封装的api移植出来供Java调用
     * 基本的包含增删改查
     * */
    private static Logger logger = LoggerFactory.getLogger(RedisShardedPoolUtil.class);

    /**
     * 增(set)
     * 在redis的set命令执行成功的话会返回字符串OK
     * */
    public static String set(String key,String value){
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            logger.error("set key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 查(get)
     * 在redis的get命令执行成功的话会返回字符串OK
     * */
    public static String get(String key){
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            logger.error("get key:{} value:{} error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }


    /**
     * 删(del)
     * */
    public static Long del(String key){
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            logger.error("del key:{} value:{} error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置key的有效期，单位是秒
     * */
    public static Long expire(String key,int exTime){
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.expire(key,exTime);
        } catch (Exception e) {
            logger.error("expire key:{} error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    //exTime的单位是秒,和setex不同，后者是一个原子性的操作(包含了expire)。
    /**
     * 为db中的数据设置生存时间，超过这个时间就会被消除
     * 同时这个key连同它的value都会被清除掉
     * */
    public static String setEx(String key,String value,int exTime){
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setex(key,exTime,value);
        } catch (Exception e) {
            logger.error("setex key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String getSet(String key,String value) {
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.getSet(key,value);
        } catch (Exception e) {
            logger.error("getset key:{} error", key, e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }


    public static Long setnx(String key,String value){
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setnx(key, value);
        } catch (Exception e) {
            logger.error("set key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(jedis);
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    //测试
    public static void main(String[] args){
        ShardedJedis jedis = RedisShardedPool.getJedis();
        RedisShardedPoolUtil.set("test1", "test1Value");
        String value = RedisShardedPoolUtil.get("test1");
        RedisShardedPoolUtil.setEx("keyex", "valueex", 60*1);
        RedisShardedPoolUtil.expire("test1", 60*2);
        RedisShardedPoolUtil.del("test1");
        System.out.println("end");
    }



}
