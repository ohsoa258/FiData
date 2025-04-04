package com.fisk.task;

import com.fisk.task.server.WebSocketServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author gy
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.task",
        "com.fisk.common.framework.mdc",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.jwt",
        "com.fisk.common.framework.properties",
        "com.fisk.common.core.user",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.hystrix",
        "com.fisk.common.framework.exception",
        "com.fisk.common.framework.actuators"},
        exclude = {RabbitAutoConfiguration.class})
@MapperScan("com.fisk.task.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.dataaccess.client",
        "com.fisk.datamodel.client",
        "com.fisk.datafactory.client",
        "com.fisk.mdm.client",
        "com.fisk.datagovernance.client",
        "com.fisk.system.client",
        "com.fisk.consumeserveice.client",
        "com.fisk.datamanage.client"})
@EnableTransactionManagement
@EnableHystrix
@EnableScheduling
@EnableAsync
public class FkTaskApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext =  SpringApplication.run(FkTaskApplication.class, args);
        WebSocketServer.setApplicationContext(configurableApplicationContext);
    }
}
