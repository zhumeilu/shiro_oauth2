package com.zml.shiro_oauth2_server.mapper;

import com.zml.shiro_oauth2_server.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface RoleMapper {

    @Select(value = "select r.* " +
            "from role r left join user_role ur on ur.roleId = r.id left join user u on u.id = ur.userId " +
            "where u.id = #{userId}")
    Set<Role> getRoleByUserId(Long userId);

}
