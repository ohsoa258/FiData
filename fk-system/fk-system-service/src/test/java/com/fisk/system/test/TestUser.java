package com.fisk.system.test;

import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.UserContext;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: Lock
 * @data: 2021/5/31 12:13
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestUser {

    @GetMapping("/me")
    public ResponseEntity<UserDetail> me(){
        return ResponseEntity.ok(UserContext.getUser());
    }

}
