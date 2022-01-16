package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.service.IApiServiceManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description api应用服务控制器
 * @date 2022/1/6 14:51
 */

@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/apiService")
public class ApiServiceController {
    @Resource
    private IApiServiceManageService service;

    @ApiOperation("获取token")
    @PostMapping("/getToken")
    public ResultEntity<Object> getToken(@RequestBody TokenDTO dto)
    {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getToken(dto));
    }

    @ApiOperation("获取数据")
    @PostMapping("/getData")
    public ResultEntity<Object> getData(HttpServletRequest request)
    {
        // 可以分页，
        String token = request.getHeader("token");
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(token));
    }
}
