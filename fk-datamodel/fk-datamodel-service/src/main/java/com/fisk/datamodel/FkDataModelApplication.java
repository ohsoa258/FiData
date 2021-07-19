package com.fisk.datamodel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.datamodel",
        "com.fisk.common.advice",
        "com.fisk.common.mdc",
        "com.fisk.common.mybatis",
        "com.fisk.common.redis",
        "com.fisk.common.user",
        "com.fisk.common.filter",
        "com.fisk.common.actuators"})
@MapperScan("com.fisk.datamodel.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client",
        "com.fisk.task.client",
        "com.fisk.dataaccess.client"})
public class FkDataModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkDataModelApplication.class, args);
    }

}
