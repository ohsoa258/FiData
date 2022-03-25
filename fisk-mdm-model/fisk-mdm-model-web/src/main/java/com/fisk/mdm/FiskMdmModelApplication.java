package com.fisk.mdm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.fisk.mdm",
        "com.fisk.common.advice",
        "com.fisk.common.mdc",
        "com.fisk.mdm.config",
        "com.fisk.common.redis",
        "com.fisk.common.feign",
        "com.fisk.common.user",
        "com.fisk.common.actuators"})
@MapperScan("com.fisk.mdm.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client"})
public class FiskMdmModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiskMdmModelApplication.class, args);
    }

}
