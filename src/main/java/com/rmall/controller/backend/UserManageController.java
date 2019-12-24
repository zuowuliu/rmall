package com.rmall.controller.backend;

import com.rmall.common.Const;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.User;
import com.rmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author 大神爱吃茶
 * */
@Controller
@RequestMapping("/manage/user/")
public class UserManageController {
    @Autowired
    IUserService iUserService;

    /**
     * 管理员登录接口
     * */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> loginAdmin(String username, String password, HttpSession session){
        ServerResponse<User> loginAdminResponse = iUserService.login(username, password);
        if(loginAdminResponse.isSuccess()){
            User currentUser = loginAdminResponse.getData();
            if(Const.IRole.ROLE_ADMIN != currentUser.getRole()){
                return ServerResponse.createByError("不是管理员，无法登录后台");
            }else {
                //说明登录的是管理员
                session.setAttribute(Const.CURRENT_USER, currentUser);
                return loginAdminResponse;
            }
        }
        return loginAdminResponse;
    }


}
