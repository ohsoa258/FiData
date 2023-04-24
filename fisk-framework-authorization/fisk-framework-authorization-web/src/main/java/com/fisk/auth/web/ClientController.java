package com.fisk.auth.web;

import com.fisk.auth.service.ClientInfoService;
import com.fisk.auth.service.IAuthenticatePushDataListService;
import com.fisk.auth.service.IAuthenticateWhiteListService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation(value = "获取秘钥")
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
    @ApiOperation(value = "路径是否存在")
    public ResultEntity<Boolean> pathIsExists(String path) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.pathIsExists(path));
    }

    @GetMapping("/pushDataPathIsExists")
    @ApiOperation(value = "推送数据路径是否存在")
    public ResultEntity<Boolean> pushDataPathIsExists(String path) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, pushDataListService.pushDataPathIsExists(path));
    }

    @GetMapping("/refreshWhiteList")
    @ApiOperation(value = "刷新白名单")
    public ResultEntity<Object> refreshWhiteList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.loadDataToRedis(null));
    }

    @GetMapping("/refreshPushDataList")
    @ApiOperation(value = "刷新推送数据列表")
    public ResultEntity<Object> refreshPushDataList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, pushDataListService.loadPushDataListToRedis(null));
    }
}
