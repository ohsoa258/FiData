package com.fisk.datamanagement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 */
/*@EnableAsync(proxyTargetClass=true)
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass=false,exposeProxy=true)*/
@SpringBootApplication(scanBasePackages = {
        "com.fisk.datamanagement",
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
@MapperScan("com.fisk.datamanagement.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client",
        "com.fisk.datamodel.client",
        "com.fisk.dataaccess.client",
        "com.fisk.datagovernance.client",
        "com.fisk.task.client",
        "com.fisk.system.client"
})
@EnableHystrix
public class FKDataManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(FKDataManagementApplication.class, args);
    }
}
