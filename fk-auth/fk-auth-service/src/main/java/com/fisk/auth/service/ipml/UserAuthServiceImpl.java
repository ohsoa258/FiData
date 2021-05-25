package com.fisk.auth.service.ipml;

import com.fisk.auth.constants.JwtConstants;
import com.fisk.auth.constants.RedisConstants;
import com.fisk.auth.dto.Payload;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.service.UserAuthService;
import com.fisk.auth.utils.JwtUtils;
import com.fisk.common.exception.FkException;
import com.fisk.common.utils.CookieUtils;
import com.fisk.user.client.UserClient;
import com.fisk.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * @param username
     * @param password
     * @param response
     */
    @Override
    public void login(String username, String password, HttpServletResponse response) {

        // 1.授权中心携带用户名密码，到用户中心(数据库)查询用户
        //请求user服务获取用户信息
        UserDTO userDTO = null;

        try {
            userDTO = userClient.queryUserByPhoneAndPassword(username, password);
        } catch (Exception e) {
            throw new FkException(400, "用户名或密码不正确");
        }

        // 3.生成JWT凭证
        // 准备数据: 自定义荷载对象
        UserDetail userDetail = UserDetail.of(userDTO.getId(), userDTO.getUsername());
        // 生成jwt: token
        String token = jwtUtils.createJwt(userDetail);

        // 4.将token写入用户Cookie
        writeCookie(response, token);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1.获取用户cookie
        String jwt = CookieUtils.getCookieValue(request, COOKIE_NAME);
        // 2.校验cookie中的token的有效性
        Payload payload = null;
        try {
            payload = jwtUtils.parseJwt(jwt);
        } catch (Exception e) {
            // 3.如果无效，什么都不做
            return;
        }
        // 4.如果有效，删除cookie（重新写一个cookie，maxAge为0）
        CookieUtils.deleteCookie(COOKIE_NAME, JwtConstants.DOMAIN, response);

        // 5.删除redis中的JTI
        // 5.1.获取用户信息
        UserDetail userDetail = payload.getUserDetail();
        // 5.2.删除redis数据
        redisTemplate.delete(RedisConstants.JTI_KEY_PREFIX + userDetail.getId());
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
