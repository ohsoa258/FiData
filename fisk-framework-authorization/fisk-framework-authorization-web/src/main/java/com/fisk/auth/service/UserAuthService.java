package com.fisk.auth.service;

import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.auth.dto.ssologin.TicketInfoDTO;
import com.fisk.common.core.response.ResultEntity;

import javax.servlet.http.HttpServletRequest;

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
     *
     * @param request 请求参数
     */
    void logout(HttpServletRequest request);

    /**
     * 获取token
     *
     * @param dto dto
     * @return 获取token结果
     */
    ResultEntity<String> getToken(UserAuthDTO dto);

    /**
     * 浦东应急局--单点登录
     *
     * @param ticketInfoDTO
     * @return
     */
    ResultEntity<String> singleLogin(TicketInfoDTO ticketInfoDTO);
}
