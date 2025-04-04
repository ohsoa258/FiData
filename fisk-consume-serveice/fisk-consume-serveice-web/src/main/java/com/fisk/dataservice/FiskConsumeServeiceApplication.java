package com.fisk.dataservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author: Lock
 * @data: 2021/5/25 10:53
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.dataservice",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mdc",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.hystrix",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.jwt",
        "com.fisk.common.framework.properties",
        "com.fisk.common.framework.exception",
        "com.fisk.common.framework.actuators",
        "com.fisk.common.service.pageFilter",
        "com.fisk.common.core.user",
        "pd.tangqiao"})
@MapperScan({"com.fisk.dataservice.mapper","pd.tangqiao.mapper"})
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client",
        "com.fisk.system.client",
        "com.fisk.dataaccess.client",
        "com.fisk.datamodel.client",
        "com.fisk.mdm.client",
        "com.fisk.task.client",
        "com.fisk.datafactory.client",
        "com.fisk.datamanage.client"
})
@EnableHystrix
@EnableAsync
public class FiskConsumeServeiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FiskConsumeServeiceApplication.class, args);
    }

}
