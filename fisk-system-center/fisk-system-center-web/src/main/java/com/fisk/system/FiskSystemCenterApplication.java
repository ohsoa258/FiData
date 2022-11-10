package com.fisk.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.system",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.config",
        "com.fisk.common.framework.properties",
        "com.fisk.common.service.pageFilter",
        "com.fisk.common.framework.exception",
        "com.fisk.common.core.user",})
@MapperScan("com.fisk.system.mapper")
@EnableFeignClients(basePackages = "com.fisk.auth.client")
@EnableHystrix
public class FiskSystemCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiskSystemCenterApplication.class,args);
    }

}
