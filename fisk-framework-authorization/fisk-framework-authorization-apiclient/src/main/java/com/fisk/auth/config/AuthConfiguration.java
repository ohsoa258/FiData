package com.fisk.auth.config;

import com.fisk.auth.client.AuthClient;
import com.fisk.auth.utils.JwtUtils;
import com.fisk.common.framework.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;

/**
 * @author Lock
 * @date 2021/5/17 15:21
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "fk.auth", name = {"clientId", "secret"})
@EnableConfigurationProperties(ClientProperties.class)
public class AuthConfiguration {

    @Resource
    private AuthClient authClient;
    @Resource
    private ClientProperties properties;

    @Bean
    @Primary
    public JwtUtils jwtUtils(RedisUtil redis){
        try {
            // 查询秘钥
            String key = authClient.getSecretKey(properties.getClientId(), properties.getSecret());
            // 创建JwtUtils
            JwtUtils jwtUtils = new JwtUtils(key, redis);
            log.info("秘钥加载成功。");
            return jwtUtils;
        } catch (Exception e) {
            log.error("初始化JwtUtils失败，{}", e.getMessage());
            throw e;
        }
    }

}
