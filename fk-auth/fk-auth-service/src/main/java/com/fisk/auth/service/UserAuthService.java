package com.fisk.auth.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: Lock
 * @data: 2021/5/17 13:48
 */
public interface UserAuthService {
    void login(String username, String password, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}
