package com.zml.shiro_oauth2_server.config;

import com.zml.shiro_oauth2_server.mapper.PermissionMapper;
import com.zml.shiro_oauth2_server.mapper.RoleMapper;
import com.zml.shiro_oauth2_server.mapper.UserMapper;
import com.zml.shiro_oauth2_server.model.Permission;
import com.zml.shiro_oauth2_server.model.Role;
import com.zml.shiro_oauth2_server.model.User;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class MyShiroRealm extends AuthorizingRealm {

    Logger logger = LoggerFactory.getLogger(MyShiroRealm.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        logger.info("-------进入doGetAuthorizationInfo----------");
        //获取用户名
        String username = (String) principalCollection.getPrimaryPrincipal();
        //根据用户名查询用户
        User user = userMapper.selectUserByUsername(username);
        if(user!=null){
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

            Set<Role> roleSet = roleMapper.getRoleByUserId(user.getId());
            if(roleSet!=null&&!roleSet.isEmpty()){
                roleSet.forEach(role->{
                    simpleAuthorizationInfo.addRole(role.getName());

                    Set<Permission> permissionSet = permissionMapper.getPermissionsByRoleId(role.getId());
                    if(permissionSet!=null&&!permissionSet.isEmpty()){
                        permissionSet.forEach(permission -> {
                            simpleAuthorizationInfo.addStringPermission(permission.getUrl());

                        });

                    }
                });
            }
            return simpleAuthorizationInfo;

        }

        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        String username = (String) authenticationToken.getPrincipal();
        String password = new String((char[]) authenticationToken.getCredentials());
        //根据用户名查询用户信息
        User user = userMapper.selectUserByUsername(username);;

        if (null == user) {
            throw new UnknownAccountException();
        }
        if(!password.equals(user.getPassword())){
            throw new IncorrectCredentialsException();
        }
//        if (!upmsUser.getPassword().equalsIgnoreCase(CoreCodecUtils.encryptMD5((password + upmsUser.getSalt())))) {
//            throw new IncorrectCredentialsException();
//        }
//        if (upmsUser.getLocked() == 1) {
//            throw new LockedAccountException();
//        }
        return new SimpleAuthenticationInfo(username, password, getName());
    }
}
