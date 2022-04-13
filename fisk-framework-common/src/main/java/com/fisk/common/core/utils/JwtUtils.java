package com.fisk.common.core.utils;

import com.fisk.common.core.constants.SystemConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * @author gy
 * @version 1.0
 * @description jwt帮助类
 * @date 2022/4/8 17:48
 */
@Slf4j
public class JwtUtils {

    /**
     * 获取token中的用户id
     *
     * @return id
     */
    public static long getUserIdByToken(String secret, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();
        Jws<Claims> claimsJws;
        try {
            claimsJws = parser.parseClaimsJws(token);
        } catch (Exception ex) {
            log.error("解析Token失败，Token: " + token, ex);
            return 0L;
        }
        Claims claims = claimsJws.getBody();
        return Long.parseLong(claims.get(SystemConstants.CLAIM_ATTR_ID, String.class));
    }
}
