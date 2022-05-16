package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.api.ApiParameterDTO;
import com.fisk.dataaccess.service.IApiParameter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-04-26 11:07:14
 */
@Api(tags = SwaggerConfig.API_PARAMETER)
@RestController
@RequestMapping("/apiParameter")
public class ApiParameterController {

    @Autowired
    private IApiParameter service;

    @GetMapping("/getList/{apiId}")
    @ApiOperation(value = "根据apiId查询请求参数集合")
    public ResultEntity<List<ApiParameterDTO>> getListByApiId(@PathVariable("apiId") long apiId) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getListByApiId(apiId));
    }

    @PostMapping("/addList")
    @ApiOperation(value = "添加API请求参数集合")
    public ResultEntity<Object> addData(@Validated @RequestBody List<ApiParameterDTO> dtoList) {

        return ResultEntityBuild.build(service.addData(dtoList));
    }

    @PutMapping("/editList")
    @ApiOperation(value = "修改API请求参数集合")
    public ResultEntity<Object> editData(@Validated @RequestBody List<ApiParameterDTO> dtoList) {

        return ResultEntityBuild.build(service.editData(dtoList));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除当前请求参数")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }

}
