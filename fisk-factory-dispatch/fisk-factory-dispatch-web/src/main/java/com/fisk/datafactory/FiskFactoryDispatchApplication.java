package com.fisk.datafactory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Lock
 * @date 2021/5/26 10:32
 * <p>
 * //@EnableJwtVerification
 */
@SpringBootApplication(scanBasePackages = {
        "com.fisk.datafactory",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mdc",
        "com.fisk.common.framework.mybatis",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.exception",
        "com.fisk.common.framework.actuators",
        "com.fisk.common.service.pageFilter",
        "com.fisk.common.core.user"})
@MapperScan("com.fisk.datafactory.mapper")
@EnableFeignClients(basePackages = {
        "com.fisk.auth.client",
        "com.fisk.dataaccess.client",
        "com.fisk.task.client",
        "com.fisk.datamodel.client"
})
@EnableTransactionManagement
@EnableHystrix
public class FiskFactoryDispatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiskFactoryDispatchApplication.class, args);
    }

}
