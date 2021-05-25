package com.fisk.chartvisual;

import com.fisk.auth.annotation.EnableJwtVerification;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 * @data: 2021/5/25 10:53
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.chartvisual", "com.fisk.common.advice"})
@MapperScan("com.fisk.chartvisual.mapper")
@EnableFeignClients(basePackages = "com.fisk.auth.client")
//@EnableJwtVerification // 开启自定义的jwt验证开关
public class FkChartVisualApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkChartVisualApplication.class, args);
    }

}
