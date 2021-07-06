package com.fisk.auth.web;

import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.auth.service.UserAuthService;
import com.fisk.common.response.ResultEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lock
 * <p>
 * 对外提供登录服务
 */
@RestController
@RequestMapping("/user")
public class UserAuthController {

    @Resource
    private UserAuthService userAuthService;

    /**
     * 登录
     * @param dto 请求参数
     * @return 返回值
     */
    @PostMapping("/login")
    public ResultEntity<String> login(
            @RequestBody UserAuthDTO dto) {

        return userAuthService.login(dto);
    }

    /**
     * 退出登录
     *
     * @param request  请求参数
     * @param response 响应数据，写cookie用
     * @return 无
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        userAuthService.logout(request, response);
        return ResponseEntity.noContent().build();
    }
}
