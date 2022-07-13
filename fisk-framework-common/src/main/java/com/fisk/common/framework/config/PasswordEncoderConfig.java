package com.fisk.common.framework.config;

import com.fisk.common.framework.properties.ProjectProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.security.SecureRandom;

/**
 * @author gy
 * @date 2022/7/13
 */
@Configuration
public class PasswordEncoderConfig {

    @Resource
    private ProjectProperties properties;

    /**
     * 加密工具: 数据库中采用的是Bcrypt算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 利用密钥生成随机安全码
        SecureRandom secureRandom = new SecureRandom(properties.getEncoder().getCrypt().secret.getBytes());
        // 初始化BCryptPasswordEncoder
        return new BCryptPasswordEncoder(properties.getEncoder().getCrypt().getStrength(), secureRandom);
    }


}
