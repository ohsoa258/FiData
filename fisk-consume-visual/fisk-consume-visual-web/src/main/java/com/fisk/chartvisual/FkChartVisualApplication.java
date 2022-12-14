package com.fisk.chartvisual;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 * @data: 2021/5/25 10:53
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.chartvisual",
        "com.fisk.common.framework.advice",
        "com.fisk.common.framework.mdc",
        "com.fisk.chartvisual.config",
        "com.fisk.common.framework.redis",
        "com.fisk.common.framework.feign",
        "com.fisk.common.framework.hystrix",
        "com.fisk.common.core.user",
        "com.fisk.common.framework.actuators"})
@MapperScan("com.fisk.chartvisual.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client", "com.fisk.task.client", "com.fisk.datamodel.client"})
@EnableHystrix
public class FkChartVisualApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkChartVisualApplication.class, args);
    }

}
