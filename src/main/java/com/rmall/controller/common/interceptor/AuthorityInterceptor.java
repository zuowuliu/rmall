package com.rmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.rmall.common.Const;
import com.rmall.common.ServerResponse;
import com.rmall.pojo.User;
import com.rmall.util.CookieUtil;
import com.rmall.util.JsonUtil;
import com.rmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 大神爱吃茶
 * @Date 2020/1/10 0010 下午 21:38
 */
public class AuthorityInterceptor implements HandlerInterceptor {

    private static Logger logger = LoggerFactory.getLogger(AuthorityInterceptor.class);

    //前置方法(用于登录管理员的权限验证)(返回true就会执行controller里面的方法，false就不会执行其中的方法)
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取当前拦截的请求映射的方法
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        //解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();
        //封装参数之后再装进此buffer中
        StringBuffer requestParamBuffer = new StringBuffer();
        //封装请求参数
        Map paramMap = request.getParameterMap();
        //entrySet是一个关系映射对象集合,整体的key-value对为一个关系映射对象
        Iterator it = paramMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            String mapKey = (String) entry.getKey();
            String mapValue = "";
            //request的这个参数map的value返回的是一个String[]
            Object obj = entry.getValue();
            if(obj instanceof String[]){
                String[] strs = (String[])obj;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        //对于拦截其中拦截manage下的login.do的处理，对于登录不拦截，直接放行
        if(StringUtils.equals(className, "UserManageController") && StringUtils.equals(methodName, "login")){
            logger.info("权限拦截器拦截到请求，className:{},methodName:{}",className,methodName);
            return true;
        }
        //对于登录的请求不拦截，获取登录的用户的信息
        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isNotEmpty(loginToken)){
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr, User.class);
        }


        //权限判断，不为管理员就没有权限处理业务后面的逻辑
        if(user == null || (user.getRole().intValue() != Const.IRole.ROLE_ADMIN)){
            //返回false，即不会调用controller里面的方法
            response.reset();//这里要添加reset，否则报异常 getWriter() has already been called for this response(response已经被其他对象调用过了)
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter out = response.getWriter();
            //上传由于富文本的控件要求，要特殊处理返回值，这里面区分是否登录以及是否有权限
            if(user == null){
                if(StringUtils.equals(className, "ProductManageController") && (StringUtils.equals(methodName, "richTextUpload"))){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg","请登录管理员");
                    out.print(JsonUtil.obj2String(resultMap));
                }else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByError("拦截器拦截，用户未登录")));
                }
            }else {
                if(StringUtils.equals(className,"ProductManageController") && (StringUtils.equals(methodName,"richTextUpload"))){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","无权限操作");
                    out.print(JsonUtil.obj2String(resultMap));
                }else{
                    out.print(JsonUtil.obj2String(ServerResponse.createByError("拦截器拦截,用户无权限操作")));
                }
            }
            out.flush();//这里要关闭流
            out.close();
            //这里虽然已经输出，但是还会走到controller，所以要return false
            return false;
        }

        return true;
    }

    //后置方法
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.info("postHandle");
    }

    //完成方法
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.info("afterCompletion");
    }
}
