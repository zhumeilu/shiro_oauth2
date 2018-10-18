package com.zml.shiro_oauth2_server.mapper;

import com.zml.shiro_oauth2_server.model.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface PermissionMapper {

    @Select(value = "select p.* " +
            "from permission p left join role_permission rp on rp.permissionId = p.id left join role r on r.id = rp.roleId " +
            "where r.id = #{roleId}")
    Set<Permission> getPermissionsByRoleId(Long roleId);


}
