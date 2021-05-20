package com.fisk.auth.web;

import com.fisk.auth.service.ClientInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Lock
 * @data: 2021/5/17 15:04
 */
@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientInfoService clientInfoService;

    /**
     * 调用此接口,获取秘钥fk-auth-service配置文件中的秘钥(fk.jwt.key)
     * @param clientId 服务名称
     * @param secret 每个服务自己的秘钥
     * @return
     */
    @GetMapping("/key")
    public ResponseEntity<String> getSecretKey(
            @RequestParam("clientId") String clientId,
            @RequestParam("secret") String secret) {

        return ResponseEntity.ok(clientInfoService.getSecretKey(clientId,secret));
    }

}
