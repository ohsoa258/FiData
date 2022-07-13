package com.fisk.common.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Lock
 * @date 2021/5/17 11:36
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 加密工具: 数据库中采用的是Bcrypt算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
