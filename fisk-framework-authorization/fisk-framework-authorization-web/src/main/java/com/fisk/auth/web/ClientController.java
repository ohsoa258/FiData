package com.fisk.auth.web;

import com.fisk.auth.service.ClientInfoService;
import com.fisk.auth.service.IAuthenticatePushDataListService;
import com.fisk.auth.service.IAuthenticateWhiteListService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fisk.auth.config.SwaggerConfig;

import javax.annotation.Resource;

/**
 * @author Lock
 * @date 2021/5/17 15:04
 */
@Api(tags = {SwaggerConfig.Client})
@RestController
@RequestMapping("/client")
public class ClientController {

    @Resource
    private ClientInfoService clientInfoService;
    @Resource
    private IAuthenticateWhiteListService service;
    @Resource
    private IAuthenticatePushDataListService pushDataListService;

    @GetMapping("/key")
    public ResponseEntity<String> getSecretKey(
            @RequestParam("clientId") String clientId,
            @RequestParam("secret") String secret) {

        return ResponseEntity.ok(clientInfoService.getSecretKey(clientId, secret));
    }

//    @GetMapping("/pathIsExists")
//    public ResponseEntity<Boolean> pathIsExists(String path) {
//        return ResponseEntity.ok(service.pathIsExists(path));
//    }

    @GetMapping("/pathIsExists")
    public ResultEntity<Boolean> pathIsExists(String path) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.pathIsExists(path));
    }

    @GetMapping("/pushDataPathIsExists")
    public ResultEntity<Boolean> pushDataPathIsExists(String path) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, pushDataListService.pushDataPathIsExists(path));
    }

    @GetMapping("/refreshWhiteList")
    public ResultEntity<Object> refreshWhiteList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.loadDataToRedis(null));
    }

    @GetMapping("/refreshPushDataList")
    public ResultEntity<Object> refreshPushDataList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, pushDataListService.loadPushDataListToRedis(null));
    }
}
