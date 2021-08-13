package com.fisk.system;

import com.fisk.auth.annotation.EnableJwtVerification;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.system",
        "com.fisk.common.advice",
        "com.fisk.common.mybatis",
        "com.fisk.common.redis",
        "com.fisk.common.exception",
        "com.fisk.common.user",})
@MapperScan("com.fisk.system.mapper")
@EnableFeignClients(basePackages = "com.fisk.auth.client")
@EnableJwtVerification
public class FkSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkSystemApplication.class,args);
    }

}
