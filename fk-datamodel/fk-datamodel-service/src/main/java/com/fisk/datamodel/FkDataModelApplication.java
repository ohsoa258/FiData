package com.fisk.datamodel;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
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
        "com.fisk.common.exception",
        "com.fisk.common.actuators"},
        exclude = {RabbitAutoConfiguration.class})
@MapperScan("com.fisk.datamodel.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client",
        "com.fisk.task.client",
        "com.fisk.dataaccess.client"})
@EnableApolloConfig
public class FkDataModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkDataModelApplication.class, args);
    }

}
