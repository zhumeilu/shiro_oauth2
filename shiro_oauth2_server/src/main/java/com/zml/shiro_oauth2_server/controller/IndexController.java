package com.zml.shiro_oauth2_server.controller;

import com.zml.shiro_oauth2_server.mapper.PermissionMapper;
import com.zml.shiro_oauth2_server.mapper.RoleMapper;
import com.zml.shiro_oauth2_server.mapper.UserMapper;
import com.zml.shiro_oauth2_server.model.Permission;
import com.zml.shiro_oauth2_server.model.Role;
import com.zml.shiro_oauth2_server.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Set;

//@Lazy
@Controller
public class IndexController {
    Logger logger = LoggerFactory.getLogger(IndexController.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @PostConstruct
    public void postConstruct(){
        System.out.println("-----postConstruct-------");
    }

    @PreDestroy
    public void preDestroy(){
        System.out.println("-----preDestroy-------");
    }

    @RequestMapping("/index")
    public String indexPage(){
        return "index";
    }

    @RequestMapping("/")
    @ResponseBody
    public Object index(){
        logger.info("--------index-----------");
        logger.error("--------index-----------");
        User zml = userMapper.selectUserByUsername("zml");
        System.out.println(zml);

        if(zml!=null){

            Set<Role> roleByUserId = roleMapper.getRoleByUserId(zml.getId());
            System.out.println(roleByUserId);

            roleByUserId.forEach(role -> {
                Set<Permission> permissionsByRoleId = permissionMapper.getPermissionsByRoleId(role.getId());
                System.out.println(permissionsByRoleId);
            });
        }

        return "hello world";
    }
}
