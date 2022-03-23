package com.fisk.auth.config;

import com.fisk.auth.utils.JwtUtils;
import com.fisk.common.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Lock
 * @date 2021/5/17 11:36
 */
@Configuration
public class JwtConfig {

    /**
     * 配置的私钥
     */
    @Value("${fk.jwt.key}")
    private String key;

    /**
     * 注入到spring容器中
     * @param redis redis帮助类
     * @return 帮助类
     */
    @Bean
    public JwtUtils jwtUtils(RedisUtil redis) {
        return new JwtUtils(key, redis);
    }

    /**
     * 加密工具: 数据库中采用的是Bcrypt算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
