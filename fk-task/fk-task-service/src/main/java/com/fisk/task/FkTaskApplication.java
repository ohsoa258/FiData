package com.fisk.task;

import com.fisk.task.server.WebSocketServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
        "com.fisk.common.actuators"})
@MapperScan("com.fisk.task.mapper")
@EnableFeignClients(basePackages = {"com.fisk.dataaccess.client"})
public class FkTaskApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext =  SpringApplication.run(FkTaskApplication.class, args);
        WebSocketServer.setApplicationContext(configurableApplicationContext);
    }
}
