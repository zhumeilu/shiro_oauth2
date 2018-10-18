package com.zml.shiro_oauth2_server.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequiresPermissions("/user/list")
    @RequestMapping("/list")
    public Object list(){
        return "userList";
    }

//    @RequiresPermissions("/user/save")
    @RequestMapping("/save")
    public Object save(){

        System.out.println(1/0);
        return "userList";
    }
}
