package com.fisk.common.framework.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.jwt.model.Payload;
import com.fisk.common.framework.jwt.model.UserDetail;
import com.fisk.common.framework.properties.ProjectProperties;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @author Lock
 * @date 2021/5/17 11:09
 * <p>
 * Jwt工具类: 用于生成、验证并解析Jwt
 */
@Component
public class JwtUtils {

    @Resource
    private ProjectProperties properties;

    @Resource
    private RedisUtil redis;

    /**
     * JWT解析器
     */
    private JwtParser jwtParser;
    /**
     * 秘钥
     */
    private SecretKey secretKey;

    private final static ObjectMapper MAPPER = new ObjectMapper();

    @PostConstruct
    public void init() {
        // 生成秘钥
        secretKey = Keys.hmacShaKeyFor(properties.getJwt().getKey().getBytes(Charset.forName("UTF-8")));
        // JWT解析器
        this.jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
    }

    /**
     * 生成jwt，自己指定的JTI
     *
     * @param userDetail 用户信息
     * @return JWT
     */
    public String createJwt(UserDetail userDetail) {
        try {
            // 1.生成jti
            String jti = createJti();
            // 2.生成token
            return Jwts.builder().signWith(secretKey)
                    .setId(jti)
                    .claim(SystemConstants.CLAIM_ATTR_USERINFO, MAPPER.writeValueAsString(userDetail))
                    .claim(SystemConstants.CLAIM_ATTR_ID, userDetail.getId().toString())
                    .compact();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析jwt，并将用户信息转为指定的Clazz类型
     *
     * @param jwt token
     * @return 载荷，包含JTI和用户信息
     */
    public Payload parseJwt(String jwt) {
        Jws<Claims> claimsJws;
        // 1.验证并解析jwt
        try {
            claimsJws = jwtParser.parseClaimsJws(jwt);
        } catch (Exception ex) {
            throw new RuntimeException("token格式不正确");
        }
        Claims claims = claimsJws.getBody();
        // 2.解析载荷数据
        Payload payload = new Payload();
        payload.setJti(claims.getId());
        payload.setId(Long.parseLong(claims.get(SystemConstants.CLAIM_ATTR_ID, String.class)));
        UserDetail userDetail;
        try {
            userDetail = MAPPER.readValue(claims.get(SystemConstants.CLAIM_ATTR_USERINFO, String.class), UserDetail.class);
            payload.setUserDetail(userDetail);
        } catch (IOException e) {
            throw new RuntimeException("用户信息解析失败！");
        }

        // 3.验证是否过期
        // 3.1.取出redis中jwt
        UserInfo userInfo = (UserInfo) redis.get(RedisKeyBuild.buildLoginUserInfo(payload.getId()));
        // 3.3.比较
        if (userInfo == null || StringUtils.isEmpty(userInfo.token)) {
            throw new RuntimeException("登录已经过期!");
        }
        if (!userInfo.token.replace(SystemConstants.AUTH_TOKEN_HEADER, "").equals(jwt)) {
            // 有token，但是不是当前jwt，说明其它人登录把当前jwt踢下去了
            throw new RuntimeException("token失效，用户可能在其它设备登录!");
        }
        return payload;
    }

    /**
     * 解析jwt，并将用户信息转为指定的Clazz类型
     *
     * @param jwt token
     * @return 载荷，包含JTI和用户信息
     */
    public Payload parseClientRegisterJwt(String jwt) {
        Jws<Claims> claimsJws;
        // 1.验证并解析jwt
        try {
            claimsJws = jwtParser.parseClaimsJws(jwt);
        } catch (Exception ex) {
            throw new RuntimeException("token格式不正确");
        }
        Claims claims = claimsJws.getBody();
        // 2.解析载荷数据
        Payload payload = new Payload();
        payload.setJti(claims.getId());
        payload.setId(Long.parseLong(claims.get(SystemConstants.CLAIM_ATTR_ID, String.class)));
        UserDetail userDetail;
        try {
            userDetail = MAPPER.readValue(claims.get(SystemConstants.CLAIM_ATTR_USERINFO, String.class), UserDetail.class);
            payload.setUserDetail(userDetail);
        } catch (IOException e) {
            throw new RuntimeException("客户端信息解析失败！");
        }

        // 3.验证是否过期
        // 3.1.取出redis中jwt
        UserInfo userInfo = (UserInfo) redis.get(RedisKeyBuild.buildClientInfo(payload.getId()));
        // 3.3.比较
        if (userInfo == null || StringUtils.isEmpty(userInfo.token)) {
            throw new RuntimeException("token失效，请重新登录!");
        }
        if (!userInfo.token.replace(SystemConstants.AUTH_TOKEN_HEADER, "").equals(jwt)) {
            // 有token，但是不是当前jwt，说明token已过期，或者用户重新登录，token有变化
            throw new RuntimeException("token失效，请重新登录!");
        }
        return payload;
    }

    private String createJti() {
        return StringUtils.replace(UUID.randomUUID().toString(), "-", "");
    }

    /**
     * 刷新jwt有效期
     *
     * @param userId 用户id
     */
    public void refreshJwt(Long userId) {
        redis.expire(RedisKeyBuild.buildLoginUserInfo(userId), RedisKeyEnum.AUTH_USERINFO.getValue());
    }

    /**
     * 设置token用不过期
     *
     * @param userId 用户id
     */
    public void permanentToken(Long userId) {
        redis.expire(RedisKeyBuild.buildLoginUserInfo(userId), -1L);
    }
}
