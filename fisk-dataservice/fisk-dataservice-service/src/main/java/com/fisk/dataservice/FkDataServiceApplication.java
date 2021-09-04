package com.fisk.dataservice;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 * @data: 2021/5/25 10:53
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.dataservice",
        "com.fisk.common.advice",
        "com.fisk.common.mdc",
        "com.fisk.common.mybatis",
        "com.fisk.common.constants",
        "com.fisk.common.redis",
        "com.fisk.common.user",
        "com.fisk.common.actuators"})
@MapperScan("com.fisk.dataservice.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client", "com.fisk.task.client","com.fisk.datamodel.client"})
@EnableApolloConfig
public class FkDataServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkDataServiceApplication.class, args);
    }

}
