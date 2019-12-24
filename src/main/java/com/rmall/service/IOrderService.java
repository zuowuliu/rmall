package com.rmall.service;

import com.github.pagehelper.PageInfo;
import com.rmall.common.ServerResponse;
import com.rmall.vo.OrderVo;

import java.util.Map;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/18 0018 下午 22:45
 */
public interface IOrderService {

    //前台方法

    ServerResponse pay(Long orderNo,Integer userId,String path);

    ServerResponse alipayCallback(Map<String,String> params);

    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);

    ServerResponse createOrder(Integer userId, Integer shippingId);

    ServerResponse cancelOrder(Integer userId, long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse getOrderDetail(Integer userId, long orderNo);

    ServerResponse list(Integer userId, Integer pageNum, Integer pageSize);

    //后台方法

    ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize);

    ServerResponse<OrderVo> manageDetail(long orderNo);

    ServerResponse<PageInfo> manageSearch(long orderNo, Integer pageNum, Integer pageSize);

    ServerResponse<String> manageSendGoods(long orderNo);
}
