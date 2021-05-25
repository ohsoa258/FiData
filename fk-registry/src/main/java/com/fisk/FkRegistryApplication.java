package com.fisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author: Lock
 * @data: 2021/5/14 15:01
 */
@SpringBootApplication
@EnableEurekaServer // 开启Eureka注册中心服务
public class FkRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkRegistryApplication.class);
    }

}
