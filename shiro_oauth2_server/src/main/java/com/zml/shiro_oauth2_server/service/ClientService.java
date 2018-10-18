package com.zml.shiro_oauth2_server.service;

import com.zml.shiro_oauth2_server.model.Client;

import java.util.List;

public interface ClientService {

    public Client createClient(Client client);// 创建客户端

    public Client updateClient(Client client);// 更新客户端

    public void deleteClient(Long clientId);// 删除客户端

    Client findOne(Long clientId);// 根据 id 查找客户端

    List<Client> findAll();// 查找所有

    Client findByClientId(String clientId);// 根据客户端 id 查找客户端

    Client findByClientSecret(String clientSecret);//根据客户端安全 KEY 查找客户端

}
