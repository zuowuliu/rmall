package com.rmall.controller.backend;

import com.rmall.common.Const;
import com.rmall.common.ResponseCode;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.Category;
import com.rmall.pojo.User;
import com.rmall.service.ICategoryService;
import com.rmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author 大神爱吃茶
 * */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    IUserService iUserService;
    @Autowired
    ICategoryService iCategoryService;


    /**
     * 增加节点接口
     * */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") int parentId){
        //判断用户登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前无用户登录，请登录后重试");
        }
        //判断登录的当前用户是否是管理员
        ServerResponse ifCurrentUserIsAdminResponse = iUserService.checkCurrentUserIfAdminUser(user);
        if(!ifCurrentUserIsAdminResponse.isSuccess()){
            return ServerResponse.createByError("当前登录用户不是管理员，无操作分类权限");
        }
        //进行到这了，当前登录的用户是管理员
        return iCategoryService.addCategory(categoryName, parentId);
    }

    /**
     * 更新节点名字
     * */
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> updateCategoryName(HttpSession session,Integer categoryId,String categoryName){
        //判断登录的用户是否是管理员
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前无用户登录，请登录后重试");
        }
        //判断登录的当前用户是否是管理员
        ServerResponse ifCurrentUserIsAdminResponse = iUserService.checkCurrentUserIfAdminUser(user);
        if(!ifCurrentUserIsAdminResponse.isSuccess()){
            return ServerResponse.createByError("当前登录用户不是管理员，无操作分类权限");
        }
        //更新分类的名称
        return iCategoryService.updateCategoryName(categoryId,categoryName);
    }

    /**
     *获取子节点目录的信息(传参为需要获取子节点的父节点ID)
     * */
    @RequestMapping(value = "get_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<List<Category>> getChildParallelCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        //判断登录的用户是否是管理员
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前无用户登录，请登录后重试");
        }
        //判断登录的当前用户是否是管理员
        ServerResponse ifCurrentUserIsAdminResponse = iUserService.checkCurrentUserIfAdminUser(user);
        if(!ifCurrentUserIsAdminResponse.isSuccess()){
            return ServerResponse.createByError("当前登录用户不是管理员，无操作分类权限");
        }
        return iCategoryService.getChildParallelCategory(categoryId);
    }


    /**
     * 获取当前目录的id，并且递归其子节点的id
     * */
    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Integer>> getCategoryIdAndDeepChildCategoryId(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        //判断登录的用户是否是管理员
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前无用户登录，请登录后重试");
        }
        //判断登录的当前用户是否是管理员
        ServerResponse ifCurrentUserIsAdminResponse = iUserService.checkCurrentUserIfAdminUser(user);
        if(!ifCurrentUserIsAdminResponse.isSuccess()){
            return ServerResponse.createByError("当前登录用户不是管理员，无操作分类权限");
        }
        return iCategoryService.getCategoryIdAndDeepChildCategoryId(categoryId);
    }

//    /**
//     * 获取当前目录的id，并且递归其子节点的id
//     * */
//    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.POST)
//    @ResponseBody
//    public ServerResponse<List<Integer>> getCategoryIdAndDeepChildCategoryId(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
//        //判断登录的用户是否是管理员
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
//        if(user == null){
//            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前无用户登录，请登录后重试");
//        }
//        //判断登录的当前用户是否是管理员
//        ServerResponse ifCurrentUserIsAdminResponse = iUserService.checkCurrentUserIfAdminUser(user);
//        if(!ifCurrentUserIsAdminResponse.isSuccess()){
//            return ServerResponse.createByError("当前登录用户不是管理员，无操作分类权限");
//        }
//        return iCategoryService.getCategoryIdAndDeepChildCategoryId(categoryId);
//    }

}
