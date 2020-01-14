package com.rmall.common;

import com.rmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 大神爱吃茶
 * @Date 2020/1/8 0008 下午 22:44
 */
public class RedisShardedPool {
    //jedis封装好了连接池对象，首先使用连接池对象,这里使用分片连接池对象
    private static ShardedJedisPool shardedJedisPool;
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
    private static String redis1Ip= PropertiesUtil.getProperty("redis1.ip");
    //初始化连接池对象使用的port
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));


    //初始化连接池对象使用的ip
    private static String redis2Ip= PropertiesUtil.getProperty("redis2.ip");
    //初始化连接池对象使用的port
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));


    //初始化JedisPool
    private static void initPool(){
        //无论是哪一种连接池对象都是使用JedisPoolConfig的对象来配置
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        //当连接耗尽的时候设置阻塞，false会抛出异常，true阻塞直到超时，默认为true，但是如果超时的话也会抛出超时异常的
        config.setBlockWhenExhausted(true);

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip, redis1Port, 1000*2);//超时时间2秒(连接超时)
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 1000*2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);
        //初始化连接池对象(放包含了可用redis信息的List)
        shardedJedisPool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);

    }

    //初始化jedis连接池(类被加载时就初始化)(初始化就可以建立一些连接)
    static{
        initPool();
    }

    //(1)在连接池中获取jedis连接(主要是提供给该类调用，借用)
    public static ShardedJedis getJedis(){
        return shardedJedisPool.getResource();
    }
    //(2)在使用完了之后归还jedis连接(返还)
    public static void returnResource(ShardedJedis shardedJedis){
        shardedJedisPool.returnResource(shardedJedis);//源码已进行空判断
    }
    //(3)返回已破坏的jedis连接
    public static void returnBrokenResource(ShardedJedis shardedJedis){
        shardedJedisPool.returnBrokenResource(shardedJedis);
    }


    //测试
    public static void main(String[] args){
        ShardedJedis shardedJedis = shardedJedisPool.getResource();

        for(int i =0;i<10;i++){
            shardedJedis.set("key"+i,"value"+i);
        }
        returnResource(shardedJedis);

//        pool.destroy();//临时调用，销毁连接池中的所有连接
        System.out.println("program is end");
    }
}
