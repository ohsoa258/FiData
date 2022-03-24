package com.fisk.chartvisual;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class LogStashTest {

    @Value("${spring.application.name}")
    public String name;

    @Test
    public void CreateLog(){
        System.out.println(name);
        log.debug("======ELK2测试=======");
        log.info("======ELK2测试=======");
        log.warn("======ELK2测试=======");
        log.error("======ELK2测试=======");
    }
}
