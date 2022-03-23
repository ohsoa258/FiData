package com.fisk.task;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.fisk.task.server.WebSocketServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author gy
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.task",
        "com.fisk.common.mdc",
        "com.fisk.common.mybatis",
        "com.fisk.common.redis",
        "com.fisk.common.user",
        "com.fisk.common.feign",
        "com.fisk.common.exception",
        "com.fisk.common.actuators"},
exclude = {RabbitAutoConfiguration.class})
@MapperScan("com.fisk.task.mapper")
@EnableFeignClients(basePackages = {"com.fisk.dataaccess.client","com.fisk.datamodel.client","com.fisk.datafactory.client"})
@EnableApolloConfig
public class FkTaskApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext =  SpringApplication.run(FkTaskApplication.class, args);
        WebSocketServer.setApplicationContext(configurableApplicationContext);
    }
}
