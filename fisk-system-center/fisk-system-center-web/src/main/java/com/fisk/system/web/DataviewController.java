package com.fisk.system.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.DataViewAddDTO;
import com.fisk.system.dto.DataViewDTO;
import com.fisk.system.dto.DataViewEditDTO;
import com.fisk.system.enums.serverModuleTypeEnum;
import com.fisk.system.service.DataviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2021/11/3 14:48
 */
@Api(tags = {SwaggerConfig.DATAVIEW})
@RestController
@RequestMapping("/dataview")
public class DataviewController {


    @Resource
    DataviewService dataviewService;

    @GetMapping("/get")
    @ApiOperation("获取个人视图和系统视图")
    public ResultEntity<Page<DataViewDTO>> get(Integer currentPage, Integer pageSize, serverModuleTypeEnum type) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataviewService.queryAll(currentPage,pageSize,type));
    }

    @PostMapping("/add")
    @ApiOperation("添加数据视图")
    public ResultEntity<Object> add(@Validated @RequestBody DataViewAddDTO dto) {
        return ResultEntityBuild.build(dataviewService.saveView(dto));
    }

    @PutMapping("/update")
    @ApiOperation("更新数据视图")
    public ResultEntity<Object> update(@Validated @RequestBody DataViewEditDTO dto) {
        return ResultEntityBuild.build(dataviewService.updateView(dto));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除视图")
    public ResultEntity<Object> delete(Integer id) {
        return ResultEntityBuild.build(dataviewService.deleteView(id));
    }
}
