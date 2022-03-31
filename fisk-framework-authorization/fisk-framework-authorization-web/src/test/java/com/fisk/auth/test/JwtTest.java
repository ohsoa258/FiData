package com.fisk.auth.test;

import com.fisk.auth.dto.Payload;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.JwtUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: Lock
 * @data: 2021/5/17 11:41
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class JwtTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void test() throws InternalError {

        // 生成jwt
        String jwt = jwtUtils.createJwt(UserDetail.of(1L, "Jack"));
        System.out.println("jwt = " + jwt);
        // 解析jwt
        Payload payload = jwtUtils.parseJwt(jwt);
        System.out.println("payload = " + payload);
    }


}
