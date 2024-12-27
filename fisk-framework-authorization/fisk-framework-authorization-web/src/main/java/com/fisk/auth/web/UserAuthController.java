package com.fisk.auth.web;

import com.fisk.auth.config.SwaggerConfig;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.auth.dto.ssologin.TicketInfoDTO;
import com.fisk.auth.service.UserAuthService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Lock
 * <p>
 * 对外提供登录服务
 */
@Api(tags = {SwaggerConfig.UserAuth})
@RestController
@RequestMapping("/user")
@Slf4j
public class UserAuthController {

    @Resource
    private UserAuthService userAuthService;

    /**
     * 登录
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @ApiOperation(value = "登录")
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
    @ApiOperation(value = "退出")
    @ControllerAOPConfig(printParams = false)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        userAuthService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "获取Token")
    @PostMapping("/getToken")
    public ResultEntity<String> getToken(
            @RequestBody UserAuthDTO dto) {

        return userAuthService.getToken(dto);
    }

    /**
     * 浦东应急局--单点登录
     *
     * @param ticketInfoDTO 请求参数
     * @return 返回值
     */
    @ApiOperation(value = "浦东应急局--单点登录")
    @PostMapping("/singleLogin")
    public ResultEntity<String> singleLogin(@RequestBody TicketInfoDTO ticketInfoDTO) {
        return userAuthService.singleLogin(ticketInfoDTO);
    }

    /**
     * 强生交通--单点登录
     *
     * @return 返回值 accesstoken
     */
    @ApiOperation(value = "强生交通--单点登录")
    @PostMapping("/qsLogin")
    public ResultEntity<String> qsLogin(@RequestParam("token") String token) {
        return userAuthService.qsLogin(token);
    }


    /**
     * Azure AD--单点登录
     *
     * @return 返回值 accesstoken
     */
    @ApiOperation(value = "Azure AD--单点登录")
    @PostMapping("/azureAdLogin")
    public ResultEntity<String> azureAdLogin(@RequestParam("code") String code) {
        return userAuthService.azureAdLogin(code);
    }

}
