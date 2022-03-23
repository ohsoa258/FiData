package com.fisk.chartvisual;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class HikariCPTest {

    AtomicInteger resultCount = new AtomicInteger(0);

    HikariDataSource ds = new HikariDataSource() {{
        setPoolName("测试mysql");
        setJdbcUrl("jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false");
        setUsername("root");
        setPassword("root123");
        //最小连接数
        setMinimumIdle(3);
        //最大连接数
        setMaximumPoolSize(10);
        //连接池中，每个连接空闲时长（超过时间删除）
        setIdleTimeout(5000);
        //等待连接池最大时间，超过时间没有连接可用的话，会抛出异常
        setConnectionTimeout(5000);
        //验证连接是否有效
        setConnectionTestQuery("select 1");
    }};
    HashMap<Integer, HikariDataSource> map = new HashMap<Integer, HikariDataSource>() {{
        put(1, ds);
    }};

    @Test
    public void CreatePool() throws InterruptedException {

        IntStream.rangeClosed(1, 20).parallel().forEach(e -> {
            query(map.get(1));
        });
        System.out.println("执行成功条数：" + resultCount.get());
        ds.close();
    }

    private void query(HikariDataSource ds) {
        try {
            Connection connection = ds.getConnection();
            Thread.sleep(getrandom(1000, 5000));
            connection.close();
            resultCount.getAndAdd(1);
            System.out.println("【" + LocalDateTime.now() + "】【ThreadName: " + Thread.currentThread().getName() + "】");
        } catch (SQLException | InterruptedException throwables) {
            throwables.printStackTrace();
        }
    }

    private static int getrandom(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }
}
