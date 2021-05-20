package com.fisk.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 * @data: 2021/5/17 11:46
 */
@EnableFeignClients(basePackages = "com.fisk.user.client") // 开启feign接口调用,并指定调用具体的接口所在的包(包扫描)
@MapperScan("com.fisk.auth.mapper") // mybatis-plus的包扫描
@SpringBootApplication(scanBasePackages = {"com.fisk.auth","com.fisk.common.advice"})
public class FkAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkAuthApplication.class);
    }
}
