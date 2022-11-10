package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.oraclecdc.CdcHeadConfigDTO;
import com.fisk.dataaccess.dto.table.TableAccessNonDTO;
import com.fisk.dataaccess.dto.table.TableKeepNumberDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessQueryDTO;
import com.fisk.dataaccess.service.ITableAccess;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_6})
@RestController
@RequestMapping("/v3/tableAccess")
public class TableAccessController {

    @Resource
    ITableAccess service;

    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addTableAccessData(@RequestBody TbTableAccessDTO dto) {

        return service.addTableAccessData(dto);
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显")
    public ResultEntity<TbTableAccessDTO> getTableAccessData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableAccessData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改物理表信息&保存sql_script(ftp信息)")
    public ResultEntity<Object> editData(@RequestBody TbTableAccessDTO dto) {
        return ResultEntityBuild.build(service.updateTableAccessData(dto));
    }

    @PostMapping("/getList")
    @ApiOperation(value = "根据appId获取物理表列表")
    public ResultEntity<Page<TbTableAccessDTO>> getTableAccessListData(@RequestBody TbTableAccessQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableAccessListData(dto));
    }

    @PostMapping("/getFieldList")
    @ApiOperation(value = "获取最新版sql脚本的表字段集合")
    public ResultEntity<Object> getFieldList(@RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFieldList(dto));
    }

    @ApiOperation("oracle-cdc脚本头配置")
    @PutMapping("/cdcHeadConfig")
    public ResultEntity<Object> buildFiDataTableMetaData(@RequestBody CdcHeadConfigDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.cdcHeadConfig(dto));
    }

    @GetMapping("/getUseExistTable")
    @ApiOperation(value = "获取现有表")
    public ResultEntity<Object> getUseExistTable() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getUseExistTable());
    }

    @PostMapping("/setKeepNumber")
    @ApiOperation(value = "设置stg数据保留天数")
    public ResultEntity<Object> setKeepNumber(@RequestBody TableKeepNumberDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setKeepNumber(dto));
    }

}
