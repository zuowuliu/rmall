package com.rmall.common;

import com.rmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author 大神爱吃茶
 * @Date 2020/1/3 0003 上午 10:00
 */
public class RedisPool {

    //jedis封装好了连接池对象，首先使用连接池对象
    private static JedisPool jedisPool;
    //连接池最大可连接数量
    private static Integer maxTotal= Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    //连接池最大空闲数量
    private static Integer maxIdle=Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "20"));
    //连接池最小空闲数量
    private static Integer minIdle=Integer.parseInt(PropertiesUtil.getProperty("redos.min.idle", "20"));
    //在borrow一个jedis实例的时候，是否要进行验证操作，如果赋值true，则是可用的
    //一般将JedisPool中的连接称为连接实例
    private static Boolean testOnBorrow=Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    //在return一个jedis实例的时候，是否要进行验证操作，如果赋值true，则放回jedispool的实例是可用的
    private static Boolean testOnReturn=Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    //初始化连接池对象使用的ip
    private static String redisIp=PropertiesUtil.getProperty("redis.ip");
    //初始化连接池对象使用的port
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));


    //初始化JedisPool
    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        //当连接耗尽的时候设置阻塞，false会抛出异常，true阻塞直到超时，默认为true，但是如果超时的话也会抛出超时异常的
        config.setBlockWhenExhausted(true);

        //初始化连接池对象
        jedisPool = new JedisPool(config, redisIp, redisPort, 1000*2);
    }

    //初始化jedis连接池
    static{
        initPool();
    }

    //在连接池中获取jedis连接(主要是提供给该类调用)
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }
    //在使用完了之后归还jedis连接
    public static void returnResource(Jedis jedis){
        jedisPool.returnResource(jedis);//源码已进行空判断
    }
    //返回已破坏的jedis连接
    public static void returnBrokenResource(Jedis jedis){
        jedisPool.returnBrokenResource(jedis);
    }


    //测试
    public static void main(String[] args){
        Jedis jedis = jedisPool.getResource();
        jedis.set("firstKey", "firstValue");
        returnResource(jedis);
        jedisPool.destroy();
        System.out.println("program is end");
    }

}
