package com.fisk.apiservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.fisk.apiservice",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mdc",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.hystrix",
        "com.fisk.common.core.user",
        "com.fisk.common.framework.actuators"})
@MapperScan("com.fisk.apiservice.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.system.client"
})
@EnableHystrix
public class FkApiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkApiServiceApplication.class, args);
    }
}
