package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetAddDTO;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetPageResultDTO;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetQueryDTO;
import com.fisk.dataaccess.service.IDataTarget;
import com.fisk.dataaccess.vo.output.datatarget.DataTargetVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_TARGET})
@RestController
@RequestMapping("/DataTarget")
public class DataTargetController {
    @Resource
    IDataTarget service;

    @PostMapping("/getDataList")
    @ApiOperation(value = "获取业务域数据列表")
    public ResultEntity<Page<DataTargetPageResultDTO>> getDataList(@RequestBody DataTargetQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataList(query));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "获取目标数据表字段")
    public ResultEntity<Object> getBusinessColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataTargetColumn());
    }

    @PostMapping("/addDataTarget")
    @ApiOperation(value = "新增数据目标")
    public ResultEntity<Object> addDataTarget(@RequestBody DataTargetAddDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addDataTarget(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除数据目标")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {
        return ResultEntityBuild.build(service.delete(id));
    }

    @GetMapping("/getDataTarget/{id}")
    @ApiOperation(value = "获取数据目标详情")
    public ResultEntity<Object> getDataTarget(@PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataTarget(id));
    }

    @PutMapping("/updateDataTarget")
    @ApiOperation(value = "编辑数据目标")
    public ResultEntity<Object> updateDataTarget(@RequestBody DataTargetVO vo) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.updateDataTarget(vo));
    }

}
