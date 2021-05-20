package com.fisk.user.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Lock
 * @data: 2021/5/17 15:50
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("测试登录");
    }

}
