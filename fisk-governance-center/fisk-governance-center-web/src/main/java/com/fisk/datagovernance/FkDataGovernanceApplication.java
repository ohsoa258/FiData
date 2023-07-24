package com.fisk.datagovernance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Lock
 * @date 2021/5/26 10:32
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.datagovernance",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mdc",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.hystrix",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.exception",
        "com.fisk.common.framework.actuators",
        "com.fisk.common.service.pageFilter",
        "com.fisk.common.core.user"})
@MapperScan("com.fisk.datagovernance.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client",
        "com.fisk.task.client",
        "com.fisk.datamanage.client",
        "com.fisk.system.client",
        "com.fisk.dataaccess.client",
        "com.fisk.mdm.client",
        "com.fisk.datamodel.client"
})
@EnableHystrix
@EnableScheduling
public class FkDataGovernanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkDataGovernanceApplication.class, args);
    }
}
