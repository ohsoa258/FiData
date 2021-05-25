package com.fisk.user.client;

import com.fisk.user.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: Lock
 * @data: 2021/5/17 13:28
 *
 * 对外开放的是controller层里的方法,注意:
 *  1.没有方法体
 *  2.不再是ResponseEntity<param>接受参数,而是ResponseEntity<param>中的param
 *  3.方法上代表CRUD的注解不变,并且要全路径,包括controller类上的路径
 */
@FeignClient("user-service")
public interface UserClient {

    /**
     * 根据用户名和密码查询用户
     *
     * @param username
     * @param password
     * @return 前端json格式传来的,@RequestParam接对象
     */
    @GetMapping("/info")
    UserDTO queryUserByPhoneAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password);

}
