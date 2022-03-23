package com.fisk.datagovernance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 * @date 2021/5/26 10:32
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.datagovernance",
        "com.fisk.common.advice",
        "com.fisk.common.mdc",
        "com.fisk.common.mybatis",
        "com.fisk.common.feign",
        "com.fisk.common.redis",
        "com.fisk.common.exception",
        "com.fisk.common.actuators",
        "com.fisk.common.filter",
        "com.fisk.common.user"})
@MapperScan("com.fisk.datagovernance.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client"
})
public class FkDataGovernanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkDataGovernanceApplication.class, args);
    }

}
