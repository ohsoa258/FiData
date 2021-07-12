package com.fisk.datamodel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.datamodel", "com.fisk.common.advice", "com.fisk.common.redis"})
@MapperScan("com.fisk.datamodel.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client", "com.fisk.task.client"})
public class FkDataModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkDataModelApplication.class, args);
    }

}
