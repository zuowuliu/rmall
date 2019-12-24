package com.rmall.service;

import com.rmall.common.ServerResponse;
import com.rmall.vo.CartVo;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/9 0009 上午 9:35
 */
public interface ICartService{
    ServerResponse<CartVo> add(Integer userId, Integer count , Integer productId);

    ServerResponse<CartVo> update(Integer userId, Integer productId , Integer count);

    ServerResponse<CartVo> deleteProduct(Integer userId, String productIds);

    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer productId, int checked);

    ServerResponse<Integer> getCartProductCount(Integer userId);
}
