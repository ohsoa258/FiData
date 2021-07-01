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

    ResultEntity<String> login(UserAuthDTO userAuthDTO);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
