package com.fisk.common.user;

import com.fisk.common.constants.SystemConstants;
import com.fisk.common.redis.RedisKeyBuild;
import com.fisk.common.redis.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * 当前登录用户帮助类
 *
 * @author gy
 */
@Component
public class UserHelper {

    @Resource
    RedisUtil redis;

    /**
     * 获取当前登录的用户
     *
     * //@param redis redis帮助类
     * @return 用户信息
     */
    public UserInfo getLoginUserInfo() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH).replace(SystemConstants.AUTH_TOKEN_HEADER, "");
        SecretKey key = Keys.hmacShaKeyFor("helloWorldJavaLeyouAuthServiceSecretKey".getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return (UserInfo) redis.get(RedisKeyBuild.buildLoginUserInfo(Long.parseLong(claims.get("id", String.class))));
    }
}
