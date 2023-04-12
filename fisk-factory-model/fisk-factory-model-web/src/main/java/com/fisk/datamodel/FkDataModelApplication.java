package com.fisk.datamodel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.datamodel",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mdc",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.hystrix",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.framework.redis",
        "com.fisk.common.core.user",
        "com.fisk.common.service.pageFilter",
        "com.fisk.common.framework.exception",
        "com.fisk.common.framework.actuators"},
        exclude = {RabbitAutoConfiguration.class})
@MapperScan("com.fisk.datamodel.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client",
        "com.fisk.task.client",
        "com.fisk.dataaccess.client",
        "com.fisk.datafactory.client",
        "com.fisk.system.client",
        "com.fisk.datamanage.client"
})
@EnableHystrix
public class FkDataModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkDataModelApplication.class, args);
    }

}
