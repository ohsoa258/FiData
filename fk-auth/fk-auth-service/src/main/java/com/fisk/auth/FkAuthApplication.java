package com.fisk.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 * @date 2021/5/17 11:46
 *
 * 开启feign接口调用,并指定调用具体的接口所在的包(包扫描)
 * mybatis-plus的包扫描
 */
@EnableFeignClients(basePackages = "com.fisk.system.client")
@MapperScan("com.fisk.auth.mapper")
@SpringBootApplication(scanBasePackages = {"com.fisk.auth", "com.fisk.common.advice", "com.fisk.common.redis"})
public class FkAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkAuthApplication.class);
    }
}
