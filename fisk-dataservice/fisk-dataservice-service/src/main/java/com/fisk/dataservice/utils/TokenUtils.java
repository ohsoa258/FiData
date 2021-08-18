package com.fisk.dataservice.utils;

import com.fisk.dataservice.entity.ConfigureUserPO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

/**
 * @author WangYan
 * @date 2021/7/28 17:53
 */
public class TokenUtils {

    private static final String SECRET = "a1g2y47dg3dj59fjhhsd7cnewy73josephsonploscnewa1g2y47dg3dj59fd7cnewy73josephsonplo47dg3dj59fdkkt";

    /**
     * 解析token
     * @param token
     */
    public static ConfigureUserPO analysisToken(String token){
        Jws<Claims> claimsJws;
        try{
            claimsJws = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token);
        }catch (Exception e){
            throw new RuntimeException("token格式不正确或者已经过期" + e);
        }

        try{
            Claims claims1 = claimsJws.getBody();
            ConfigureUserPO configureUser = new ConfigureUserPO();
            configureUser.setId(claims1.get("id", Long.class));
            configureUser.setUserName(claims1.get("username", String.class));
            return configureUser;
        }catch (Exception e){
            throw new RuntimeException("用户信息解析失败！" + e);
        }
    }

    /**
     * 生成token
     * @param user 用户信息
     * @return
     */
    public static String createJwt(ConfigureUserPO user) {
        // 1.生成jti
        String jti = createJti();
        // 2.生成token
        return Jwts.builder().signWith(SignatureAlgorithm.HS512,SECRET)
                .setId(jti)
                .claim("id", user.getId())
                .claim("username", user.getUserName())
                .compact();
    }

    private static String createJti() {
        return StringUtils.replace(UUID.randomUUID().toString(), "-", "");
    }
}
