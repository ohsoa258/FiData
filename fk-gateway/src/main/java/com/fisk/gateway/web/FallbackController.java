package com.fisk.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Lock
 * @data: 2021/5/14 15:14
 */
@RestController
public class FallbackController {
    /**
     *
     * 默认的超时时间提醒: 504状态
     * @return
     */
    @GetMapping("/hystrix/fallback")
    public ResponseEntity<String> fallbackController() {

        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("请求超时!");
    }
}
