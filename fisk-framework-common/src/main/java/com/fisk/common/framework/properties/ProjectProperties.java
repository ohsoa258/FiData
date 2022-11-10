package com.fisk.common.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author gy
 */
@Data
@Component
@ConfigurationProperties("fk")
public class ProjectProperties {

    private AuthProperties auth;
    private JwtProperties jwt;
    private EncoderProperties encoder;

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

    @Data
    public static class EncoderProperties {
        private CryptProperties crypt;

        @Data
        public static class CryptProperties {
            public String secret;
            public int strength;
        }
    }
}
