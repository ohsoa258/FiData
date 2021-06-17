package com.fisk.auth.service.impl;

import com.fisk.auth.constants.JwtConstants;
import com.fisk.auth.dto.Payload;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.service.UserAuthService;
import com.fisk.auth.utils.JwtUtils;
import com.fisk.common.constants.SystemConstants;
import com.fisk.common.exception.FkException;
import com.fisk.common.redis.RedisKeyBuild;
import com.fisk.common.redis.RedisKeyEnum;
import com.fisk.common.redis.RedisUtil;
import com.fisk.common.user.UserInfo;
import com.fisk.user.client.UserClient;
import com.fisk.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.fisk.auth.constants.JwtConstants.COOKIE_NAME;

/**
 * @author: Lock
 * @data: 2021/5/17 13:53
 */
@Service
public class UserAuthServiceImpl implements UserAuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtUtils jwtUtils;

    @Resource
    private RedisUtil redis;

    /**
     * @param username
     * @param password
     * @param response
     */
    @Override
    public String login(String username, String password, HttpServletResponse response) {

        // 1.授权中心携带用户名密码，到用户中心(数据库)查询用户
        //请求user服务获取用户信息
        UserDTO userDTO = null;

        try {
            userDTO = userClient.queryUser(username, password);
        } catch (Exception e) {
            throw new FkException(400, "用户名或密码不正确");
        }

        // 创建自定义荷载对象
        UserDetail userDetail = UserDetail.of(userDTO.getId(), userDTO.getUsername());
        // 生成jwt: token
        String token = SystemConstants.AUTH_TOKEN_HEADER + jwtUtils.createJwt(userDetail);
        UserInfo userInfo = UserInfo.of(userDTO.getId(), userDTO.getUsername(), token);
        boolean res = redis.set(RedisKeyBuild.buildLoginUserInfo(userInfo.id), userInfo, RedisKeyEnum.AUTH_USERINFO.getValue());

        return token;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1.获取token
        String token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH).replace(SystemConstants.AUTH_TOKEN_HEADER, "");
        // 2.校验token的有效性
        Payload payload = null;
        try {
            payload = jwtUtils.parseJwt(token);
        } catch (Exception e) {
            // 3.如果无效，什么都不做
            return;
        }
        UserDetail userDetail = payload.getUserDetail();
        // 4删除redis数据
        redis.del(RedisKeyBuild.buildLoginUserInfo(userDetail.getId()));
    }

    private void writeCookie(HttpServletResponse response, String token) {
        // cookie的作用域
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(JwtConstants.DOMAIN);
        // 是否禁止JS操作cookie，避免XSS攻击
        cookie.setHttpOnly(true);
        // cookie有效期，-1就是跟随当前会话，浏览器关闭就消失
        cookie.setMaxAge(-1);
        // cookie作用的路径，/代表一切路径
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
