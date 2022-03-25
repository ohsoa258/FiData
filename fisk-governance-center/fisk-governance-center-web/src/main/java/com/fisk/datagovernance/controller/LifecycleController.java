package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleEditDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleQueryDTO;
import com.fisk.datagovernance.service.dataquality.ILifecycleManageService;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期
 * @date 2022/3/22 16:15
 */
@Api(tags = {SwaggerConfig.TAG_5})
@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {
    @Resource
    private ILifecycleManageService service;

    @ApiOperation("分页查询生命周期模板组件")
    @PostMapping("/page")
    public ResultEntity<Page<LifecycleVO>> getAll(@RequestBody LifecycleQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("添加生命周期模板组件")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody LifecycleDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑生命周期模板组件")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody LifecycleEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("删除生命周期模板组件")
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }
}
