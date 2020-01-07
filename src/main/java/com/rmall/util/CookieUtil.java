package com.rmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * @author 大神爱吃茶
 * @Date 2020/1/6 0006 下午 20:04
 */
public class CookieUtil {
    private static Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    private final static String COOKIE_DOMAIN = ".rmall.com";
    private final static String COOKIE_NAME = "rmall_login_token";

    //从请求中读取cookie
    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for(Cookie ck : cks){
                logger.info("read cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                if(StringUtils.equals(ck.getName(), COOKIE_NAME)){
                    logger.info("return cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    //往响应中写cookie
    public static void writeLoginToken(HttpServletResponse response,String token){
        //new Cookie的时候是通过key-value的形式来new的
        //这里的cookie的名字就是rmall_login_token，而值就是token,这里的token就是sessionID,装在一级域名下的
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        //将cookie设置在根目录下面
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        //设置cookie的有效期，单位是秒(一年)
        //如果这个maxage不设置的话，cookie就不会写入硬盘，而是写在内存。只在当前页面有效。
        cookie.setMaxAge(60*60*24*365);
        logger.info("write cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
        response.addCookie(cookie);
    }

    //删除cookie(从请求中读，往响应中写,已经删除完了的)
    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(StringUtils.equals(cookie.getName(), COOKIE_NAME)){
                    cookie.setDomain(COOKIE_DOMAIN);
                    cookie.setPath("/");
                    //删除的关键步骤
                    cookie.setMaxAge(0);
                    logger.info("del cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                    response.addCookie(cookie);
                    return;
                }
            }
        }
    }

}
