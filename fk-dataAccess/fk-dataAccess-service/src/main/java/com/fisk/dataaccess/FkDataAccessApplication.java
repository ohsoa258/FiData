package com.fisk.dataaccess;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
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
@SpringBootApplication(scanBasePackages = {
        "com.fisk.dataaccess",
        "com.fisk.common.advice",
        "com.fisk.common.mdc",
        "com.fisk.common.mybatis",
        "com.fisk.common.feign",
        "com.fisk.common.redis",
        "com.fisk.common.exception",
        "com.fisk.common.actuators",
        "com.fisk.common.filter",
        "com.fisk.common.user"})
@MapperScan("com.fisk.dataaccess.mapper")
@EnableFeignClients(basePackages = {"com.fisk.auth.client", "com.fisk.task.client"})
//@Configuration
@EnableApolloConfig
public class FkDataAccessApplication {

//    private static String[] args;
//    private static ConfigurableApplicationContext context;


    public static void main(String[] args) {
//        FkDataAccessApplication.args = args;
        /*FkDataAccessApplication.context = */SpringApplication.run(FkDataAccessApplication.class, args);
    }

//    public static void restart() {
//        context.close();
//        FkDataAccessApplication.context = SpringApplication.run(FkDataAccessApplication.class, args);
//    }
}
