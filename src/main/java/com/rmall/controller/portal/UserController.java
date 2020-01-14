package com.rmall.controller.portal;

import com.rmall.common.Const;
import com.rmall.common.ResponseCode;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.User;
import com.rmall.service.IUserService;
import com.rmall.util.CookieUtil;
import com.rmall.util.JsonUtil;
import com.rmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value="/user/")
public class UserController {

    /**
     * 用户登录
     * @大神爱吃茶
     * */

    @Autowired
    private IUserService iUserService;


    /**
     * 1、登录接口
     * */
    @RequestMapping(value = "login.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> login(String username, String password,HttpSession session,HttpServletResponse response){

        ServerResponse<User> serverResponse = iUserService.login(username, password);
        if(serverResponse.isSuccess()){
            //同时将user的信息添加进session中(重要)，替换为将user的信息添加进redis缓存中并设置失效时间
            //session.setAttribute(Const.CURRENT_USER, serverResponse.getData());

            //这里将sessionID当做token写进了cookie中并传进response中，并且cookie的key是rmall_login_token，值为token即sessionID
            CookieUtil.writeLoginToken(response, session.getId());
            RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(serverResponse.getData()), Const.RedisCacheExtime.REDIS_SESSION_EXTIME);

        }
        return serverResponse;
    }
    /**
     * 2、退出登录接口
     * */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> loginOut(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse){
        //session.removeAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        //这一步让浏览器中保存下来的cookie消失
        CookieUtil.delLoginToken(httpServletRequest, httpServletResponse);
        //这一步让redis中的loginToken消失
        RedisShardedPoolUtil.del(loginToken);
        return ServerResponse.createBySuccess("退出登录成功","退出登录");
    }

    /**
     * 3、注册接口
     * */
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        ServerResponse<String> stringServerResponse = iUserService.register(user);
        return stringServerResponse;
    }

    /**
     * 4、校验
     * */
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String value,String type){
        return iUserService.checkValid(value,type);
    }

    /**
     * 获取登录用户的信息接口
     * */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        //User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }else {
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
    }

    /**
     * 忘记密码接口，返回这个用户的问题给他
     * */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetPasswordGetQuestion(String username){
        ServerResponse forgetPwdGetQuestionResponse = iUserService.selectUserQuestion(username);
        return forgetPwdGetQuestionResponse;
    }

    /**
     * 校验问题的答案是否准确接口
     * */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetPasswordCheckAnswer(String username,String question,String answer){
        return  iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 忘记密码，重置密码接口
     * */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetPasswordRestPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetPwdRestPwd(username,passwordNew,forgetToken);
    }

    /**
     * 登录状态下的重置密码接口
     * */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> LoginStatusRestPassword(String passwordOld,String passwordNew,HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError("用户未登录");
        }
        return iUserService.LoginStatusRestPwd(passwordOld,passwordNew,user);
    }

    /**
     * 更新信息接口
     * */

    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpServletRequest httpServletRequest,User user){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr,User.class);

        if(currentUser == null){
            return ServerResponse.createByError("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            RedisShardedPoolUtil.setEx(loginToken, JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }


    /**
     * 登录状态下获取当前用户的详细信息反馈给前台接口
     * */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInformation(HttpServletRequest httpServletRequest){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr, User.class);
        if(currentUser == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，需登录后才可查看");
        }
        //如果不为空就将当前用户的信息给前台
        return iUserService.getUserInformation(currentUser.getId());
    }





}
