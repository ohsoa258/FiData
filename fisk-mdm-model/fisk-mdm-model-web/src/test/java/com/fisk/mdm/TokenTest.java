package com.fisk.mdm;

import com.fisk.common.core.user.UserHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;

/**
 * @author gy
 * @version 1.0
 * @description TODO
 * @date 2022/4/13 15:01
 */
// @SpringBootTest
// @RunWith(SpringRunner.class)
public class TokenTest {

    @Resource
    UserHelper userHelper;
    @Value("${fk.jwt.key}")
    String secret;

    @Test
    public void TokenTest() {
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJjMTZkMTlmM2UzYjE0ZDM4YWRiM2E4ZGVlMmE3Njc5ZSIsInVzZXIiOiJ7XCJpZFwiOjgwLFwidXNlckFjY291bnRcIjpcImd1b3l1XCJ9IiwiaWQiOiI4MCJ9.Eou9qttgV9FXJ07HwYE8IrmhoBgNtieVH0nGXhzAe84";
        String token2 = "1Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJjMTZkMTlmM2UzYjE0ZDM4YWRiM2E4ZGVlMmE3Njc5ZSIsInVzZXIiOiJ7XCJpZFwiOjgwLFwidXNlckFjY291bnRcIjpcImd1b3l1XCJ9IiwiaWQiOiI4MCJ9.Eou9qttgV9FXJ07HwYE8IrmhoBgNtieVH0nGXhzAe84";
        // long id = JwtUtils.getUserIdByToken(secret, token);
        if (!token.equals(token2)) {
            System.out.println("1");
        }
    }
}
