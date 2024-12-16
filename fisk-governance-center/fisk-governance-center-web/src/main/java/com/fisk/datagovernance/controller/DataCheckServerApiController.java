package com.fisk.datagovernance.controller;

import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.service.dataquality.DatacheckServerApiConfigService;
import com.fisk.datagovernance.vo.dataquality.datacheck.ApiSeverSubVO;
import com.fisk.datagovernance.dto.dataquality.datacheck.api.RequstDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.api.TokenDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: wangjian
 * @Date: 2024-10-23
 * @Description:
 */
@Api(tags = {SwaggerConfig.DATA_CHECK_SERVER})
@RestController
@RequestMapping("/datacheckapi")
public class DataCheckServerApiController {

    @Resource
    DatacheckServerApiConfigService service;

    private final HttpServletResponse response;

    public DataCheckServerApiController(HttpServletResponse response) {
        this.response = response;
    }

    @ApiOperation("获取token")
    @PostMapping("/getToken")
    public ResultEntity<Object> getToken(@RequestBody TokenDTO dto)
    {
        return service.getToken(dto);
    }

    @ApiOperation("获取数据")
    @PostMapping("/getData")
    public ResultEntity<Object> getData(@Validated @RequestBody RequstDTO dto)
    {
        return service.getData(dto);
    }

    @ApiOperation("分页查询所有api订阅")
    @PostMapping("/getApiSubAll")
    public ResultEntity<PageDTO<ApiSeverSubVO>> getApiSubAll(@RequestBody ApiSubQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getApiSubAll(dto));
    }
    @ApiOperation("修改订阅api字段")
    @PostMapping("/editApiField")
    public ResultEntity<ResultEnum> editApiField(@RequestBody ApiFieldEditDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.editApiField(dto));
    }

    @ApiOperation("修改订阅api状态")
    @PostMapping("/editApiState")
    public ResultEntity<ResultEnum> editApiState(@RequestBody ApiStateDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.editApiState(dto));
    }

    @ApiOperation("删除订阅api")
    @GetMapping("/delApi")
    public ResultEntity<ResultEnum> delApi(@RequestParam Integer apiId) {
        return ResultEntityBuild.build(service.delApi(apiId));
    }

    @ApiOperation("重置密码")
    @PostMapping("/resetPwd")
    public ResultEntity<Object> resetPwd(@Validated @RequestBody AppPwdResetDTO dto) {
        return ResultEntityBuild.build(service.resetPwd(dto));
    }

    @ApiOperation("生成文档")
    @PostMapping("/createDoc")
    @ControllerAOPConfig(printParams = false)
    public ResultEntity<Object> createDoc(@Validated @RequestBody CreateAppApiDocDTO dto) {
        return ResultEntityBuild.build(service.createDoc(dto,response));
    }
}
