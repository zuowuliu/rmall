package com.rmall.service;

import com.github.pagehelper.PageInfo;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.Shipping;

import java.util.List;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/14 0014 下午 19:50
 */
public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse del(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse<Shipping> select(Integer userId, Integer shippingId);

    ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize);
}
