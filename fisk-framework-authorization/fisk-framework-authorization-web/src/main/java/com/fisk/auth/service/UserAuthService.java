package com.fisk.auth.service;

import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.response.ResultEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lock
 */
public interface UserAuthService {
//    ResultEntity<String> login(String username, String password, HttpServletResponse response);

    /**
     * 登录
     * @param userAuthDTO dto
     * @return 登录结果
     */
    ResultEntity<String> login(UserAuthDTO userAuthDTO);

    /**
     * 登出
     * @param request 请求参数
     * @param response 响应数据，写cookie用
     */
    void logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取token
     *
     * @param dto dto
     * @return 获取token结果
     */
    ResultEntity<String> getToken(UserAuthDTO dto);
}
