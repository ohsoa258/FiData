package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckTypeV0;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 校验规则
 * @date 2022/3/22 16:17
 */
@Api(tags = {SwaggerConfig.DATA_CHECK_CONTROLLER})
@RestController
@RequestMapping("/datacheck")
public class DataCheckController {
    @Resource
    private IDataCheckManageService service;

    @ApiOperation("分页查询数据校验模板组件")
    @PostMapping("/page")
    public ResultEntity<Page<DataCheckVO>> getAll(@RequestBody DataCheckQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("添加数据校验模板组件")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody DataCheckDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑数据校验模板组件")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody DataCheckEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("删除数据校验模板组件")
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }

    @ApiOperation("界面/接口验证")
    @PostMapping("/interfaceCheckData")
    public ResultEntity<List<DataCheckResultVO>> interfaceCheckData(@Validated @RequestBody DataCheckWebDTO dto) {
        return service.interfaceCheckData(dto);
        //return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
    }

    @ApiOperation("同步验证")
    @PostMapping("/syncCheckData")
    public ResultEntity<List<DataCheckResultVO>> syncCheckData(@Validated @RequestBody DataCheckSyncDTO dto) {
        return service.syncCheckData(dto);
        //return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
    }

    @ApiOperation("获取校验类型")
    @GetMapping("/getDataCheckTypeList")
    public ResultEntity<List<DataCheckTypeV0>> getDataCheckTypeList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataCheckTypeList());
    }
}
