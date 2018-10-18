package com.zml.shiro_oauth2_server.model;

import lombok.Data;

@Data
public class Client {

    private Long id;
    private String name;
    private String secret;
}
