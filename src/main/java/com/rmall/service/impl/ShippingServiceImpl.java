package com.rmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rmall.common.ResponseCode;
import com.rmall.common.ServerResponse;
import com.rmall.dao.ShippingMapper;
import com.rmall.pojo.Shipping;
import com.rmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/14 0014 下午 19:50
 */
@Service
public class ShippingServiceImpl implements IShippingService {


    @Autowired
    private ShippingMapper shippingMapper;


    /**
     * 新增收货地址(要求在新增地址后把shipping的id返回给前端)
     * */
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        //新增了收货地址之后要让前端能直接获取到这个id，之前在前端传进来的参数里面不会有这个地址的id
        //需要在xml中设置mybatis自动生成主键
        //useGeneratedKeys="true" keyProperty="id"
        //所以此时就会在insert地址后自动生成一个id给这个对象并设为主键，同时让这个对象getId
        //此时下面的 shipping.getId() 就可以成立了,不然从开始的时候shipping是没有id的
        int resultCount = shippingMapper.insert(shipping);
        if(resultCount>0){
            //我们平时管理的地址也不止一个
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新增地址成功", result);
        }else {
            return ServerResponse.createByError("新增地址失败");
        }
    }

    /**
     * 删除地址接口
     * */
    public ServerResponse del(Integer userId, Integer shippingId) {
        //有可能用户没有勾选shippingId但是仍点了删除的按键，那就传不了shippingId的参数了，但是有也得在controller里面判断
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if(resultCount>0){
            return ServerResponse.createBySuccess("删除地址成功");
        }else {
            return ServerResponse.createByError("删除地址失败");
        }
    }

    /**
     * 更新地址
     * */
    public ServerResponse update(Integer userId, Shipping shipping) {
        //由于是更新所以此时前台又传来一个新的shipping对象，此时仍然是没有userId的
        shipping.setUserId(userId);
        int resultCount = shippingMapper.updateByShipping(shipping);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByError("更新地址失败");
    }

    /**
     * 获取用户的某一条地址的信息
     * 比如在前端页面中展示出来了一些地址的简述，想要点开某一个地址的详情页面所以就需要
     * 传进来一个shippingId来获取此条地址的信息
     * */
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId) {
        //获取到的是一个shipping对象，因为代表的是shipping地址信息,注意防止横向越权
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if(shipping==null){
            return ServerResponse.createByError("无法查询到该地址");
        }else {
            return ServerResponse.createBySuccess("查询地址信息成功",shipping);
        }
    }

    /**
     * 获取用户的所有地址列表
     * 返回的应该就是一个List对象
     * */
    public ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
