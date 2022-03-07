package com.fisk.auth.client;

import com.fisk.auth.dto.clientregister.ClientRegisterDTO;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.response.ResultEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 判断请求路径是否在白名单内
     *
     * @param path 请求地址
     * @return 返回结果
     */
    @GetMapping("/client/pushDataPathIsExists")
    public ResultEntity<Boolean> pushDataPathIsExists(@RequestParam("path") String path);

    /**
     * 获取token
     *
     * @param dto dto
     * @return 获取token结果
     */
    @PostMapping("/user/getToken")
    ResultEntity<String> getToken(@RequestBody UserAuthDTO dto);

    /**
     * 获取所有客户端信息
     *
     * @return list
     */
    @GetMapping("/clientRegister/getClientInfoList")
    public ResultEntity<List<String>> getClientInfoList();

    /**
     * 根据id查询客户端数据
     *
     * @param id 客户端id
     * @return 客户端信息
     */
    @GetMapping("/clientRegister/get/{id}")
    public ResultEntity<ClientRegisterDTO> getData(@PathVariable("id") long id);
}