package com.fisk.auth.interceptors;

import com.fisk.auth.constants.JwtConstants;
import com.fisk.auth.dto.Payload;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.JwtUtils;
import com.fisk.auth.utils.UserContext;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.utils.CookieUtils;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: Lock
 * @data: 2021/5/17 15:42
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    private final JwtUtils jwtUtils;

    public LoginInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 获取cookie中的jwt
            String jwt = CookieUtils.getCookieValue(request, JwtConstants.COOKIE_NAME);
            // 验证并解析
            Payload payload = jwtUtils.parseJwt(jwt);
            // 获取用户
            UserDetail userDetails = payload.getUserDetail();
            log.info("用户{}正在访问。", userDetails.getUsername());
            // 保存用户
            UserContext.setUser(userDetails);
            return true;
        } catch (JwtException e) {
            throw new FkException(ResultEnum.AUTH_JWT_ERROR, e);
        } catch (IllegalArgumentException e){
            throw new FkException(ResultEnum.AUTH_USER_NOTLOGIN, e);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 业务结束后，移除用户
        UserContext.removeUser();
    }
}
