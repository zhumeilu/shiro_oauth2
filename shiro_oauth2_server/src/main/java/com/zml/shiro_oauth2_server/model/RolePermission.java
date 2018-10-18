package com.zml.shiro_oauth2_server.model;

import lombok.Data;

@Data
public class RolePermission {

    private Long id;
    private Long roleId;
    private Long permissionId;
}
