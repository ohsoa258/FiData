package com.fisk.datagovern;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.datagovern",
        "com.fisk.common.advice",
        "com.fisk.common.mdc",
        "com.fisk.common.mybatis",
        "com.fisk.common.feign",
        "com.fisk.common.redis",
        "com.fisk.common.exception",
        "com.fisk.common.actuators",
        "com.fisk.common.filter",
        "com.fisk.common.user"})
@MapperScan("com.fisk.datagovern.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client"})
@EnableApolloConfig
public class FKDataGovernApplication {
    public static void main(String[] args) {
        SpringApplication.run(FKDataGovernApplication.class, args);
    }
}
