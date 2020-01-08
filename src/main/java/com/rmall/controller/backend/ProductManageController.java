package com.rmall.controller.backend;

import com.google.common.collect.Maps;
import com.rmall.common.Const;
import com.rmall.common.ResponseCode;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.Product;
import com.rmall.pojo.User;
import com.rmall.service.IFileService;
import com.rmall.service.IProductService;
import com.rmall.service.IUserService;
import com.rmall.util.CookieUtil;
import com.rmall.util.JsonUtil;
import com.rmall.util.PropertiesUtil;
import com.rmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author 大神爱吃茶
 * 商品管理controller
 * */

@Controller
@RequestMapping(value = "/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 保存商品接口（更新商品接口也是这个）
     * */
    @RequestMapping(value = "save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse saveProduct(HttpServletRequest httpServletRequest, Product product){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        //判断是否有用户登录
        if(user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "当前没有管理员登录，请登录后重试");
        }
        //判断登录的用户是否为管理员
        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
        }

        return iProductService.saveOrUpdateProduct(product);
    }

    /**
     * 设置商品的销售状态接口(决定其到底是上架还是下架处理的状态)
     * */
    @RequestMapping(value = "set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setSaleStatus(HttpServletRequest httpServletRequest,Integer productId,Integer status){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
        }
        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
        }
        return iProductService.setSaleStatus(productId,status);
    }


    /**
     * 获取商品的详情接口
     * */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse manageGetProductDetail(HttpServletRequest httpServletRequest,Integer productId){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
        }
        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
        }
        return iProductService.manageGetProductDetail(productId);
    }


    /**
     * 动态获取后台商品列表接口
     * */
    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse manageGetProductList(HttpServletRequest httpServletRequest, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
        }
        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
        }
        return iProductService.manageGetProductList(pageNum,pageSize);
    }

    /**
     * 动态获取后台商品列表接口
     * */
    @RequestMapping(value = "search.do")
    @ResponseBody
    public ServerResponse searchProduct(HttpServletRequest httpServletRequest, String productName,String productId,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
        }
        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
        }
        return iProductService.searchProduct(productName,productId,pageNum,pageSize);
    }

    /**
     * 上传文件（包含商品的信息、图片那些）接口（不包含商品的业务类）
     * */
    @RequestMapping(value = "upload.do")
    @ResponseBody
    public ServerResponse upload(HttpServletRequest httpServletRequest , @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByError("用户未登录，获取用户信息失败");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        if(user == null){
            return ServerResponse.createByError("当前没有管理员登录，请登录后重试");
        }
        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
            return ServerResponse.createByError("当前登录的用户不为管理员，无操作权限");
        }
        //获取文件上传保存的位置路径/webapp里面，与WEB-INF同级
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = iFileService.upload(file, path);//返回的是上传后的文件的名字
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix", "http://file.ren.com/")+targetFileName;

        Map fileMap = Maps.newHashMap();
        fileMap.put("uri", targetFileName);
        fileMap.put("url", url);
        return ServerResponse.createBySuccess(fileMap);
    }


    /**
     * 富文本的上传接口
     * */
    @RequestMapping(value = "richtext_img_upload.do")
    @ResponseBody
    //使用的是simditor插件（富文本插件）所以要按照simditor的返回要求来进行返回(富文本对于返回值有自己的要求)
    public Map richTextUpload(HttpServletRequest httpServletRequest, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap = Maps.newHashMap();

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isEmpty(loginToken)){
            resultMap.put("success", false);
            resultMap.put("msg", "请登录管理员");
            return resultMap;
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            resultMap.put("success", false);
            resultMap.put("msg", "请登录管理员");
            return resultMap;
        }
        if(!iUserService.checkCurrentUserIfAdminUser(user).isSuccess()){
            resultMap.put("success", false);
            resultMap.put("msg", "当前登录用户无操作权限");
            return resultMap;
        }
        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetName = iFileService.upload(file, path);
        if(StringUtils.isBlank(targetName)){
            resultMap.put("success", false);
            resultMap.put("msg", "富文本上传失败");
            return resultMap;
        }
        //上面已经上传成功，下面获取访问的链接url
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetName;
        resultMap.put("success", true);
        resultMap.put("msg", "富文本上传成功");
        resultMap.put("file_path", url);
        //和前端的一个约定，需要将响应的头上针对于富文本上传加上这样一个头信息
        response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
        return resultMap;
    }
}
