package com.fisk.dataaccess.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author: Lock
 * @data: 2021/5/27 15:24
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LogTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test() throws InternalError {

        String key = prefix();


        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        //设置起始值
        redisAtomicLong.set(1);
        // 设置redis步长增长为2
        redisAtomicLong.addAndGet(5);
        // for (int i = 0; i < 100; i++) {
        long andIncrement = redisAtomicLong.getAndIncrement();
        //设置5位，不够则前面补零
        String orderId = prefix() + String.format("%1$05d", andIncrement);
        String insertSQL = "insert into orderNumber value('" + orderId + "');";
        // System.out.println(Thread.currentThread().getName() +
        // ",insertSQL:" + insertSQL);
        if ((null == redisAtomicLong || redisAtomicLong.longValue() == 0)) {// 初始设置过期时间
            redisAtomicLong.expire(200, TimeUnit.SECONDS);
        }
        System.out.println(insertSQL);
    }

    public String prefix() {
        String temp_str = "";
        Date dt = new Date();
        // 最后的aa表示“上午”或“下午” HH表示24小时制 如果换成hh表示12小时制
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        temp_str = sdf.format(dt);
        return temp_str;
    }
}
