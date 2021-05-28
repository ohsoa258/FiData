package com.fisk.dataaccess.test;

import com.fisk.auth.dto.Payload;
import com.fisk.auth.dto.UserDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: Lock
 * @data: 2021/5/27 15:24
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LogTest {
    @Test
    public void test() throws InternalError {

        int a = 1;
        int b = 2;
        System.out.println(a + b);
    }
}
