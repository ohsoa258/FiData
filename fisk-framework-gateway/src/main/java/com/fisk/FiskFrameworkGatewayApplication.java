package com.fisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * SpringBoot、Hystrix、注册到注册中心
 * @date 2021/5/14 15:07
 * @author Lock
 */
//@SpringCloudApplication
@EnableFeignClients
@EnableHystrix
@SpringBootApplication(scanBasePackages = {"com.fisk.gateway","com.fisk.common"},
        exclude={DataSourceAutoConfiguration.class})
public class FiskFrameworkGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiskFrameworkGatewayApplication.class,args);
    }

}
