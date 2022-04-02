package com.fisk.common.core.user;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
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

    private final String secret = "helloWorldJavaLeyouAuthServiceSecretKey";

    @Resource
    RedisUtil redis;

    /**
     * 获取当前登录的用户
     *
     * @return 用户信息
     */
    public UserInfo getLoginUserInfo() {
        //获取token
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null)
        {
            return null;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH);
        if(StringUtils.isEmpty(token))
        {
            return null;
        }
        token = token.replace(SystemConstants.AUTH_TOKEN_HEADER, "");
        //解析token
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        //根据token中的id查询用户信息
        return (UserInfo) redis.get(RedisKeyBuild.buildLoginUserInfo(Long.parseLong(claims.get(SystemConstants.CLAIM_ATTR_ID, String.class))));
    }

    /**
     * 获取当前登录的用户
     *
     * @return 用户信息
     */
    public ResultEntity<UserInfo> getLoginUserInfo(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();
        Jws<Claims> claimsJws;
        try {
            claimsJws = parser.parseClaimsJws(token);
        } catch (Exception ex) {
            return ResultEntityBuild.build(ResultEnum.AUTH_TOKEN_PARSER_ERROR);
        }
        Claims claims = claimsJws.getBody();
        UserInfo userInfo = (UserInfo) redis.get(RedisKeyBuild.buildLoginUserInfo(Long.parseLong(claims.get(SystemConstants.CLAIM_ATTR_ID, String.class))));
        if (userInfo == null || StringUtils.isEmpty(userInfo.token)) {
            return ResultEntityBuild.build(ResultEnum.AUTH_JWT_ERROR);
        }
        if (!userInfo.token.replace(SystemConstants.AUTH_TOKEN_HEADER, "").equals(token)) {
            return ResultEntityBuild.build(ResultEnum.AUTH_JWT_ERROR);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, userInfo);
    }

    /**
     * 获取token中的用户id
     * @return id
     */
    public Long getUserIdByToken(String token){
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();
        Jws<Claims> claimsJws;
        try {
            claimsJws = parser.parseClaimsJws(token);
        } catch (Exception ex) {
            return null;
        }
        Claims claims = claimsJws.getBody();
        return Long.parseLong(claims.get(SystemConstants.CLAIM_ATTR_ID, String.class));
    }
}
