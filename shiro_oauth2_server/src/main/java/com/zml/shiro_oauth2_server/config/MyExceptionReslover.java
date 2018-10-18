package com.zml.shiro_oauth2_server.config;

import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMethodExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyExceptionReslover extends AbstractHandlerMethodExceptionResolver {
    @Override
    protected ModelAndView doResolveHandlerMethodException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, HandlerMethod handlerMethod, Exception e) {
        if(e instanceof AuthorizationException){
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/500");
            return modelAndView;
        }
        return null;
    }
}
