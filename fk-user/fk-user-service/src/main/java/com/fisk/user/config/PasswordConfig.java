package com.fisk.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

/**
 * @author: Lock
 * @data: 2021/5/14 17:19
 *
 * 对密码进行加密,
 */
@Data
@Configuration
// 读取配置文件中的密钥
@ConfigurationProperties(prefix = "fk.encoder.crypt")
public class PasswordConfig {

    private int strength;
    private String secret;

    // 将此方法注入到Bean对象中,以方法名取出
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        // 利用密钥生成随机安全码
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        // 初始化BCryptPasswordEncoder
        return new BCryptPasswordEncoder(strength, secureRandom);
    }

}
