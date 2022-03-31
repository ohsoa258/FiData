package com.fisk.mdm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.fisk.mdm",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mdc",
        "com.fisk.mdm.config",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.feign",
        "com.fisk.common.core.user",
        "com.fisk.common.framework.actuators"})
@MapperScan("com.fisk.mdm.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client"})
public class FiskMdmModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiskMdmModelApplication.class, args);
    }

}
