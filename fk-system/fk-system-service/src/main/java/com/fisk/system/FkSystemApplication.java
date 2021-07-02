package com.fisk.system;

import com.fisk.auth.annotation.EnableJwtVerification;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: Lock
 * @data: 2021/5/14 16:24
 */
// 第二包扫描: 为了让统一日志类生效
@SpringBootApplication(scanBasePackages = {"com.fisk.system", "com.fisk.common.advice", "com.fisk.common.redis"})
@MapperScan("com.fisk.system.mapper")
@EnableFeignClients(basePackages = "com.fisk.auth.client")
@EnableJwtVerification // 开启自定义的jwt验证开关
public class FkSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkSystemApplication.class,args);
    }

}
