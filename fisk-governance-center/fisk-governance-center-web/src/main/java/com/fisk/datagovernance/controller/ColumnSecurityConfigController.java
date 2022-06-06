package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigUserAssignmentDTO;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigValidDTO;
import com.fisk.datagovernance.service.datasecurity.ColumnSecurityConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Api(tags = SwaggerConfig.COLUMN_SECURITY_CONFIG)
@RestController
@RequestMapping("/columnSecurityConfig")
public class ColumnSecurityConfigController {

    @Resource
    private ColumnSecurityConfigService service;

    /**
     * 获取列级配置列表
     */
    @GetMapping("/getList/{tableId}")
    @ApiOperation(value = "获取列级配置列表")
    public ResultEntity<Object> getData(@PathVariable("tableId") String tableId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listColumnSecurityConfig(tableId));
    }

    /**
     * 添加
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addData(@RequestBody ColumnSecurityConfigUserAssignmentDTO dto){
        return ResultEntityBuild.build(service.saveColumnSecurityConfig(dto));
    }

    /**
     * 回显: 根据id查询数据
     */
    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<ColumnSecurityConfigDTO> getData(@PathVariable("id") long id){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    /**
     * 修改
     */
    @PutMapping("/edit")
    @ApiOperation(value = "修改")
    public ResultEntity<Object> editData(@RequestBody ColumnSecurityConfigUserAssignmentDTO dto){
        return ResultEntityBuild.build(service.editData(dto));
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }

    /**
     * 修改有效状态
     */
    @PutMapping("/updateValid")
    @ApiOperation(value = "修改有效状态")
    public ResultEntity<Object> updateValid(@RequestBody ColumnSecurityConfigValidDTO dto){
        return ResultEntityBuild.build(service.updateValid(dto));
    }


}
