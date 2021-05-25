package com.fisk.auth.web;

import com.fisk.auth.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: Lock
 * @data: 2021/5/17 13:46
 *
 * 对外提供登录服务
 */
@RestController
@RequestMapping("/user")
public class UserAuthController {

    @Autowired
    private UserAuthService userAuthService;

    /**
     * 登录
     * @param username
     * @param password
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            HttpServletResponse response) {

        userAuthService.login(username,password,response);

        // 登录成功,无返回值,204状态码
        return ResponseEntity.noContent().build();
    }

    /**
     * 退出登录
     * @param request 请求参数
     * @param response 响应数据，写cookie用
     * @return 无
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
        userAuthService.logout(request, response);
        return ResponseEntity.noContent().build();
    }
}
