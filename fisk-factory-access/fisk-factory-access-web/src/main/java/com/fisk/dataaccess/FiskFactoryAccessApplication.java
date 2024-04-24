package com.fisk.dataaccess;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Lock
 * @date 2021/5/26 10:32
 * <p>
 * //@EnableJwtVerification
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.dataaccess",
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
        "com.fisk.common.core.user"})
@MapperScan("com.fisk.dataaccess.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client",
        "com.fisk.task.client",
        "com.fisk.datamodel.client",
        "com.fisk.datafactory.client",
        "com.fisk.datagovernance.client",
        "com.fisk.datamanage.client",
        "com.fisk.system.client"
})
@EnableTransactionManagement
@EnableHystrix
@EnableScheduling
public class FiskFactoryAccessApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiskFactoryAccessApplication.class, args);
        System.out.println("项目启动");
    }

}
