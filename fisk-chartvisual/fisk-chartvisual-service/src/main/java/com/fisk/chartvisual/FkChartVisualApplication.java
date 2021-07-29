package com.fisk.chartvisual;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 * @data: 2021/5/25 10:53
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.chartvisual",
        "com.fisk.common.advice",
        "com.fisk.common.mdc",
        "com.fisk.common.mybatis",
        "com.fisk.common.redis",
        "com.fisk.common.feign",
        "com.fisk.common.user",
        "com.fisk.common.actuators"})
@MapperScan("com.fisk.chartvisual.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client", "com.fisk.task.client"})
public class FkChartVisualApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkChartVisualApplication.class, args);
    }

}
