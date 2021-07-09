package com.fisk.dataaccess;

import com.fisk.auth.annotation.EnableJwtVerification;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Lock
 * @date 2021/5/26 10:32
 * <p>
 * //@EnableJwtVerification
 */
@SpringBootApplication(scanBasePackages = {"com.fisk.dataaccess", "com.fisk.common.advice", "com.fisk.common.redis"})
@MapperScan("com.fisk.dataaccess.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client", "com.fisk.task.client"})
public class FkDataAccessApplication {
    public static void main(String[] args) {
        SpringApplication.run(FkDataAccessApplication.class, args);
    }
}
