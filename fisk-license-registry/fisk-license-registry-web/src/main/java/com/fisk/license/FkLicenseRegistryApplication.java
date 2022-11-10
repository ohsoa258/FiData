package com.fisk.license;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 * @date 2021/5/26 10:32
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.license",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mdc",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.exception",
        "com.fisk.common.framework.actuators",
        "com.fisk.common.service.pageFilter",
        "com.fisk.common.core.user"})
@MapperScan("com.fisk.datagovernance.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client"
})
@EnableHystrix
public class FkLicenseRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkLicenseRegistryApplication.class, args);
    }
}
