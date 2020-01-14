package com.rmall.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 大神爱吃茶
 * @Date 2020/1/10 0010 下午 16:53
 */
@Component
public class ExceptionResolver implements HandlerExceptionResolver {

    private static Logger logger = LoggerFactory.getLogger(ExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //说明此URI发生了异常，并把异常通过ex打印到控制台上面
        logger.error("{} Exception",request.getRequestURI(), ex);
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        modelAndView.addObject("status", ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg", "接口异常，详情请查看服务日志");
        modelAndView.addObject("data", ex.toString());
        return modelAndView;
    }
}
