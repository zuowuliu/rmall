package com.rmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.rmall.common.Const;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.User;
import com.rmall.service.IOrderService;
import com.rmall.service.IUserService;
import com.rmall.util.CookieUtil;
import com.rmall.util.JsonUtil;
import com.rmall.util.RedisShardedPoolUtil;
import com.rmall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author 大神爱吃茶
 * @Date 2019/12/23 0023 下午 23:05
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {


    @Autowired
    private IUserService iUserService;

    @Autowired
    private IOrderService iOrderService;


    /**
     * （1）后台获取订单列表接口
     *
     * */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpServletRequest httpServletRequest, @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
//        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
//        if(StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByError("用户未登录，获取用户信息失败");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.string2Obj(userJsonStr, User.class);
//        if(user == null){
//            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
//        }
//        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
//            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
//        }
        return iOrderService.manageList(pageNum,pageSize);
    }

    /**
     * （2）后台获取订单详情接口
     *
     * */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(HttpServletRequest httpServletRequest, long orderNo){
//        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
//        if(StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByError("用户未登录，获取用户信息失败");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.string2Obj(userJsonStr, User.class);
//        if(user == null){
//            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
//        }
//        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
//            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
//        }
        return iOrderService.manageDetail(orderNo);
    }

    /**
     * （3）按订单号搜索订单接口
     *
     * */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(HttpServletRequest httpServletRequest,long orderNo, @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
//        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
//        if(StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByError("用户未登录，获取用户信息失败");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.string2Obj(userJsonStr, User.class);
//        if(user == null){
//            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
//        }
//        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
//            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
//        }
        return iOrderService.manageSearch(orderNo,pageNum,pageSize);
    }

    /**
     * （4）后台发货接口
     *
     *  根据订单号来发货
     * */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpServletRequest httpServletRequest,long orderNo){
//        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
//        if(StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByError("用户未登录，获取用户信息失败");
//        }
//        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.string2Obj(userJsonStr, User.class);
//        if(user == null){
//            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
//        }
//        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
//            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
//        }
        return iOrderService.manageSendGoods(orderNo);
    }
}
