package com.fisk.common.core.user;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class UserHelper {

    @Value("${fk.jwt.key}")
    String secret;

    @Resource
    RedisUtil redis;

    /**
     * 获取当前登录的用户
     * 获取不到用户信息抛出异常
     *
     * @return 用户信息
     */
    public UserInfo getLoginUserInfo() {
        // 获取请求属性集合
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.error("获取请求中token失败，错误原因：RequestAttributes为null");
            throw new FkException(ResultEnum.NOTFOUND_REQUEST_ATTR);
        }
        // 获取request对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取token
        String token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH);
        if (StringUtils.isEmpty(token)) {
            log.error("获取请求中token失败，错误原因：请求头中缺少token");
            throw new FkException(ResultEnum.UNAUTHENTICATE, ResultEnum.AUTH_TOKEN_IS_NOTNULL.getMsg());
        }
        token = token.replace(SystemConstants.AUTH_TOKEN_HEADER, "");
        // 解析token
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // 获取claims
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        // 获取userId
        long userId = Long.parseLong(claims.get(SystemConstants.CLAIM_ATTR_ID, String.class));
        // 根据token中的id查询用户信息
        Object redisData = redis.get(RedisKeyBuild.buildLoginUserInfo(userId));
        if (redisData == null) {
            log.error("用户登录信息不存在。用户id：" + userId);
            throw new FkException(ResultEnum.UNAUTHENTICATE, ResultEnum.AUTH_LOGIN_INFO_INVALID.getMsg());
        }
        UserInfo userInfo = (UserInfo) redisData;
        // 如果请求中的token和redis中存储的token不一样，说明用户账号已经在其他地方重新登录，token已过期。
        if (!StringUtils.equals(userInfo.token.replace(SystemConstants.AUTH_TOKEN_HEADER, ""), token)) {
            log.error("token验证失败，错误原因：请求中的token与redis不一致。");
            throw new FkException(ResultEnum.UNAUTHENTICATE, ResultEnum.AUTH_LOGIN_INFO_INVALID.getMsg());
        }
        return userInfo;
    }

    /**
     * 获取当前登录的用户（mybatis的填充策略使用，获取不到用户信息会返回null）
     * 获取不到用户信息返回null
     *
     * @return 用户信息
     */
    public UserInfo getLoginUserInfoNotThrowError() {
        try {
            return getLoginUserInfo();
        } catch (Exception ex) {
            return null;
        }
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
            return ResultEntityBuild.build(ResultEnum.UNAUTHENTICATE, ResultEnum.AUTH_TOKEN_PARSER_ERROR.getMsg());
        }
        Claims claims = claimsJws.getBody();
        long userId = Long.parseLong(claims.get(SystemConstants.CLAIM_ATTR_ID, String.class));
        Object redisData = redis.get(RedisKeyBuild.buildLoginUserInfo(userId));
        if (redisData == null) {
            log.error("用户登录信息不存在。用户id：" + userId);
            return ResultEntityBuild.build(ResultEnum.UNAUTHENTICATE, ResultEnum.AUTH_LOGIN_INFO_INVALID.getMsg());
        }
        UserInfo userInfo = (UserInfo) redisData;
        if (!StringUtils.equals(userInfo.token.replace(SystemConstants.AUTH_TOKEN_HEADER, ""), token)) {
            log.error("请求中的token与登录信息中的token不一致。");
            return ResultEntityBuild.build(ResultEnum.UNAUTHENTICATE, ResultEnum.AUTH_LOGIN_INFO_INVALID.getMsg());
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, userInfo);
    }

    /**
     * 获取token中的用户id
     *
     * @return id
     */
    public Long getUserIdByToken(String token) {
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
