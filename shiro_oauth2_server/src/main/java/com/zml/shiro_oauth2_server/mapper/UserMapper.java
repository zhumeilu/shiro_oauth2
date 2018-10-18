package com.zml.shiro_oauth2_server.mapper;

import com.zml.shiro_oauth2_server.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Insert(value = "insert into `user`(name,username,password,salt) values(#{name},#{username},#{password},#{salt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);


    @Select("select * from `user` where username=#{username}")
    User selectUserByUsername(String username);

}
