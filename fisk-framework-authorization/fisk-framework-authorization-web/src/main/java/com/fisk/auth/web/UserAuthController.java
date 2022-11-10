package com.fisk.auth.web;

import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.auth.service.UserAuthService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Lock
 * <p>
 * 对外提供登录服务
 */
@RestController
@RequestMapping("/user")
@Slf4j
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
     * @param request 请求参数
     * @return 无
     */
    @ControllerAOPConfig(printParams = false)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        userAuthService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/getToken")
    public ResultEntity<String> getToken(
            @RequestBody UserAuthDTO dto) {

        return userAuthService.getToken(dto);
    }
}
