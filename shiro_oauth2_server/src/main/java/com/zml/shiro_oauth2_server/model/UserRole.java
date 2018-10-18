package com.zml.shiro_oauth2_server.model;

import lombok.Data;

@Data
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
}
