package com.fisk.dataaccess.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.api.ApiConfigDTO;
import com.fisk.dataaccess.dto.api.GenerateApiDTO;
import com.fisk.dataaccess.service.IApiConfig;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@RestController
@RequestMapping("/apiconfig")
public class ApiConfigController {

    @Resource
    private IApiConfig service;

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<ApiConfigDTO> getData(@PathVariable("id") long id){

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody ApiConfigDTO apiConfig){

        return ResultEntityBuild.build(service.addData(apiConfig));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改")
    public ResultEntity<Object> editData(@RequestBody ApiConfigDTO dto){

        return ResultEntityBuild.build(service.editData(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }

    @PutMapping("/configTable")
    @ApiOperation(value = "配置表")
    public ResultEntity<Object> configTable(@RequestBody ApiConfigDTO dto){

        return ResultEntityBuild.build(service.editData(dto));
    }

    @GetMapping("/generateApi/{id}")
    @ApiOperation(value = "根据apiId生成api文档")
    public ResultEntity<List<GenerateApiDTO>> generateApi(@PathVariable("id") long id){

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.generateApi(id));
    }

//    @PostMapping("/parseApiJson")
//    @ApiOperation(value = "解析json")
//    public ResultEntity<List<GenerateApiDTO>> parseApiJson(@RequestBody ){
//
//        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.generateApi(id));
//    }
}
