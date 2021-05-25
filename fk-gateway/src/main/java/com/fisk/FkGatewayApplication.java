package com.fisk;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 * @data: 2021/5/14 15:07
 */

/**
 * SpringBoot、Hystrix、注册到注册中心
 */
@SpringCloudApplication
@EnableFeignClients
public class FkGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkGatewayApplication.class,args);
    }

}
