package com.zml.shiro_oauth2_server.model;


import lombok.Data;

@Data
public class User {

    private Long id;
    private String name;
    private String username;
    private String password;
    private String salt;

}
