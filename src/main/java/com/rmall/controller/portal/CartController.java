package com.rmall.controller.portal;

import com.rmall.common.Const;
import com.rmall.common.ResponseCode;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.User;
import com.rmall.service.ICartService;
import com.rmall.util.CookieUtil;
import com.rmall.util.JsonUtil;
import com.rmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/9 0009 上午 9:13
 */
@Controller
@RequestMapping(value = "/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

//有关商品的接口，包含增删改查

    /**
     * 将商品添加进购物车接口
     * */
    //* 前台接口
    //参照的参数值是：传入的一个商品的数量信息、商品的id
    @RequestMapping(value = "add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(HttpServletRequest httpServletRequest, Integer count , Integer productId){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有用户登录，需要登录后再添加购物车");
        }
        //添加进对应id的用户user的购物车，指定的商品，指定的数量
        return iCartService.add(user.getId(),count,productId);
    }

    /**
     * 更新商品接口
     * */
    @RequestMapping(value = "update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse update(HttpServletRequest httpServletRequest, Integer count ,Integer productId){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有用户登录，需要登录后再添加购物车");
        }
        return iCartService.update(user.getId(),productId,count);
    }

    /**
     * 删除商品接口
     * */
    @RequestMapping(value = "delete_product.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse deleteProduct(HttpServletRequest httpServletRequest,String productIds){//传一个字符串用","分割
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有用户登录，需要登录后再添加购物车");
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    /**
     * 查找购物车的接口，直接返回用户的购物车详情
     * */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有用户登录，需要登录后再添加购物车");
        }
        return iCartService.list(user.getId());
    }

    /**
     * 全选接口
     * */
    @RequestMapping(value = "select_all.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse selectAll(HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有用户登录，需要登录后再添加购物车");
        }
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
    }

    /**
     * 全不选接口
     * */
    @RequestMapping(value = "un_select_all.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse unSelectAll(HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有用户登录，需要登录后再添加购物车");
        }
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    /**
     * 指定全选接口
     * */
    @RequestMapping(value = "select.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse select(HttpServletRequest httpServletRequest,Integer productId){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有用户登录，需要登录后再添加购物车");
        }
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
    }

    /**
     * 指定全选接口
     * */
    @RequestMapping(value = "un_select.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse unSelect(HttpServletRequest httpServletRequest,Integer productId){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有用户登录，需要登录后再添加购物车");
        }
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    /**
     * 获取当前用户的购物车产品数量接口
     * */
    @RequestMapping(value = "get_cart_product_count.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user==null){
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }
}
