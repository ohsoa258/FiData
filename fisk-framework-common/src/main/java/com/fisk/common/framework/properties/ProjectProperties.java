package com.fisk.common.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Lock
 */
@Data
@Component
@ConfigurationProperties("fk")
public class ProjectProperties {

    private final AuthProperties auth = new AuthProperties();
    private final JwtProperties jwt = new JwtProperties();

    @Data
    public static class AuthProperties {
        /**
         * 客户端id
         */
        private String clientId;
        /**
         * 客户端秘钥
         */
        private String secret;
    }

    @Data
    public static class JwtProperties {
        private String key;
    }
}
