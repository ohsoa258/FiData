package com.fisk.dataaccess;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 * @data: 2021/5/26 10:32
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.dataaccess", "com.fisk.common.advice"})
@MapperScan("com.fisk.dataaccess.mapper")
@EnableFeignClients(basePackages = "com.fisk.auth.client")
//@EnableJwtVerification // 开启自定义的jwt验证开关
public class FkDataAccessApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkDataAccessApplication.class, args);
    }
}
