package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppDTO;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppQueryDTO;
import com.fisk.dataaccess.service.IDataTargetApp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_TARGET_APP})
@RestController
@RequestMapping("/DataTargetApp")
public class DataTargetAppController {

    @Resource
    IDataTargetApp service;

    @PostMapping("/getDataList")
    @ApiOperation(value = "获取数据目标应用列表")
    public ResultEntity<Page<DataTargetAppDTO>> getDataList(@RequestBody DataTargetAppQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataTargetAppList(query));
    }

    @PostMapping("/addDataTargetApp")
    @ApiOperation(value = "新增数据目标应用")
    public ResultEntity<Object> addDataTarget(@RequestBody DataTargetAppDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addDataTargetApp(dto));
    }

    @GetMapping("/getDataTargetApp/{id}")
    @ApiOperation(value = "获取数据目标应用详情")
    public ResultEntity<Object> getDataTargetApp(@PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataTargetApp(id));
    }

    @PutMapping("/updateDataTargetApp")
    @ApiOperation(value = "编辑数据目标应用")
    public ResultEntity<Object> updateDataTargetApp(@RequestBody DataTargetAppDTO vo) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.updateDataTargetApp(vo));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除数据目标应用")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {
        return ResultEntityBuild.build(service.deleteDataTargetApp(id));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "获取目标数据应用表字段")
    public ResultEntity<Object> getBusinessColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataTargetAppColumn());
    }

}
