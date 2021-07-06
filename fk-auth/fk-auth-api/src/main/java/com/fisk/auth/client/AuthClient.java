package com.fisk.auth.client;

import com.fisk.common.response.ResultEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Lock
 * @date 2021/5/17 15:00
 */
@FeignClient("auth-service")
public interface AuthClient {

    /**
     * 提供秘钥给其他服务,最终就是为了拿到fk-auth的秘钥
     * @param clientId 微服务id
     * @param secret   微服务秘钥  用于fk-auth给其他微服务做身份验证
     * @return 秘钥对象,auth-service的配置文件application.yml中配置的
     */
    @GetMapping("/client/key")
    String getSecretKey(
            @RequestParam("clientId") String clientId,
            @RequestParam("secret") String secret);

    /**
     * 判断请求路径是否在白名单内
     * @param path 请求地址
     * @return 返回结果
     */
    @GetMapping("/client/pathIsExists")
    ResultEntity<Boolean> pathIsExists(@RequestParam("path") String path);

}