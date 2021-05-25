package com.fisk.auth.config;

import com.fisk.auth.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author: Lock
 * @data: 2021/5/17 11:36
 */
@Configuration
public class JwtConfig {

    // 配置的私钥
    @Value("${fk.jwt.key}")
    private String key;

    //注入到spring容器中
    @Bean
    public JwtUtils jwtUtils(StringRedisTemplate redisTemplate){
        return new JwtUtils(key,redisTemplate);
    }

    /**
     * 加密工具: 数据库中采用的是Bcrypt算法
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }



}
