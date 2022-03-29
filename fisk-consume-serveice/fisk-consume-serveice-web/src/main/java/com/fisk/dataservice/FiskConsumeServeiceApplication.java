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
@SpringBootApplication(scanBasePackages = {
        "com.fisk.dataservice",
        "com.fisk.common.advice",
        "com.fisk.common.mdc",
        "com.fisk.common.mybatis",
        "com.fisk.common.feign",
        "com.fisk.common.redis",
        "com.fisk.common.exception",
        "com.fisk.common.actuators",
        "com.fisk.common.filter",
        "com.fisk.common.user"})
@MapperScan("com.fisk.dataservice.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client",
        "com.fisk.task.client",
        "com.fisk.system.client"
})
@EnableApolloConfig
public class FiskConsumeServeiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FiskConsumeServeiceApplication.class, args);
    }

}
