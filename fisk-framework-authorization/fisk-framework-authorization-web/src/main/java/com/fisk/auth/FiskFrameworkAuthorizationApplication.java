package com.fisk.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 * @date 2021/5/17 11:46
 * <p>
 * 开启feign接口调用,并指定调用具体的接口所在的包(包扫描)
 * mybatis-plus的包扫描
 */
@EnableFeignClients(basePackages = "com.fisk.system.client")
@MapperScan("com.fisk.auth.mapper")
@SpringBootApplication(scanBasePackages = {
        "com.fisk.auth",
        "com.fisk.common.core.user",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.hystrix",
        "com.fisk.common.framework.jwt",
        "com.fisk.common.framework.config",
        "com.fisk.common.framework.properties",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.service.pageFilter"})
@EnableHystrix
public class FiskFrameworkAuthorizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(FiskFrameworkAuthorizationApplication.class);
    }
}
