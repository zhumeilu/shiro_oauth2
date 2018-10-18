package com.zml.shiro_oauth2_server.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login(){


        return "login";
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public Object doLogin(HttpServletRequest request,String username, String password,boolean rememberMe){
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
//        if (rememberMe) {
//            usernamePasswordToken.setRememberMe(true);
//        } else {
//            usernamePasswordToken.setRememberMe(false);
//        }
        try{
            subject.login(usernamePasswordToken);

        }catch (UnknownAccountException e){

            System.out.println("帐号不存在");
        }catch (IncorrectCredentialsException e){
            System.out.println("密码错误");
        }catch (LockedAccountException e){
            System.out.println("帐号已锁定");
        }

        System.out.println(username);
        System.out.println(password);
        return "登录成功";
    }

    @RequestMapping("/logout")
    public String logout(){
        SecurityUtils.getSubject().logout();
        return "/login";
    }
}
