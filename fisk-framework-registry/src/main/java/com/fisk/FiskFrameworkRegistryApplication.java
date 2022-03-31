package com.fisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author Lock
 */
@SpringBootApplication
@EnableEurekaServer
public class FiskFrameworkRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiskFrameworkRegistryApplication.class);
    }

}
