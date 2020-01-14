package com.rmall.common;

import com.rmall.util.PropertiesUtil;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author 大神爱吃茶
 * @Date 2020/1/14 0014 上午 10:56
 */
@Component
public class RedissonManager {
    private static Logger logger = LoggerFactory.getLogger(RedissonManager.class);
    //在redis环境没有搭建起来之前，这里先注释上，否则项目启动不起来。

    private Config config = new Config();

    private Redisson redisson = null;

    private final static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private final static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));



    //注入到Spring容器的话，使用@PostConstruct或者静态块初始化，效果是一样的{}


    @PostConstruct
    private void init() {
        try {
            //在redis环境没有搭建起来之前，这里先注释上，否则项目启动不起来。

            ////127.0.0.1:6379
//            config.useSingleServer().setAddress(new StringBuilder().append(redis1Ip).append(":").append(redis1Port).toString());


            //单主模式
//            config.useMasterSlaveServers().setMasterAddress(new StringBuilder().append(redis1Ip).append(":").append(redis1Port).toString());


            //主从模式
//            config.useMasterSlaveServers().setMasterAddress("10.211.55.6:6379").addSlaveAddress("10.211.55.6:6380");


//            redisson = (Redisson) Redisson.create(config);
            logger.info("初始化Redisson结束");
        } catch (Exception e) {
            logger.error("redisson init error", e);
        }
    }


//    {
//        init();
//    }



    public Redisson getRedisson() {
        return redisson;
    }

}
