package com.fisk.dataaccess.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.api.ApiConfigDTO;
import com.fisk.dataaccess.dto.api.ApiUserDTO;
import com.fisk.dataaccess.dto.api.GenerateDocDTO;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.service.IApiConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@Api(tags = SwaggerConfig.API_CONFIG)
@RestController
@RequestMapping("/apiConfig")
public class ApiConfigController {

    @Resource
    private IApiConfig service;

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<ApiConfigDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加api")
    public ResultEntity<Object> addData(@RequestBody ApiConfigDTO dto) {

        return ResultEntityBuild.build(service.addData(dto));
    }

    @PostMapping("/addApiDetail")
    @ApiOperation(value = "添加api下的物理表--保存or发布")
    public ResultEntity<Object> addApiDetail(@RequestBody ApiConfigDTO dto) {

        return ResultEntityBuild.build(service.addApiDetail(dto));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改api")
    public ResultEntity<Object> editData(@RequestBody ApiConfigDTO dto) {

        return ResultEntityBuild.build(service.editData(dto));
    }

    @PutMapping("/editApiDetail")
    @ApiOperation(value = "修改api下的物理表--保存or发布")
    public ResultEntity<Object> editApiDetail(@RequestBody ApiConfigDTO dto) {

        return ResultEntityBuild.build(service.editApiDetail(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除api")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }

    @GetMapping("/getList/{appId}")
    @ApiOperation(value = "根据appId获取api列表")
    public ResultEntity<List<ApiConfigDTO>> getApiListData(
            @PathVariable("appId") long appId) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getApiListData(appId));
    }

    @PostMapping("/generatePDFDocument")
    @ApiOperation(value = "生成api文档")
    public ResultEntity<Object> generateDoc(@Validated @RequestBody GenerateDocDTO dto, HttpServletResponse response) {
        return ResultEntityBuild.build(service.generateDoc(dto, response));
    }

    @PostMapping("/pushdata")
    @ApiOperation(value = "推送api数据")
    public ResultEntity<Object> pushData(@RequestBody ReceiveDataDTO dto) {
        return ResultEntityBuild.build(service.pushData(dto));
    }

    @PostMapping("/getToken")
    @ApiOperation(value = "获取实时api的临时token")
    public ResultEntity<String> getToken(@RequestBody ApiUserDTO dto) {
        return service.getToken(dto);
    }
}
