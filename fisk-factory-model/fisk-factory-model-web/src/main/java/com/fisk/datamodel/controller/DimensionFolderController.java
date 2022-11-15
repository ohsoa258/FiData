package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderPublishQueryDTO;
import com.fisk.datamodel.service.IDimensionFolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DIMENSION_FOLDER})
@RestController
@RequestMapping("/dimensionFolder")
@Slf4j
public class DimensionFolderController {

    @Resource
    IDimensionFolder service;

    @ApiOperation("添加维度文件夹")
    @PostMapping("/addDimensionFolder")
    public ResultEntity<Object> addDimensionFolder(@Validated @RequestBody DimensionFolderDTO dto) {
        return ResultEntityBuild.build(service.addDimensionFolder(dto));
    }

    @ApiOperation("删除维度文件夹")
    @DeleteMapping("/delDimensionFolder")
    public ResultEntity<Object> delDimensionFolder(@Validated @RequestBody List<Integer> dto) {
        return ResultEntityBuild.build(service.delDimensionFolder(dto));
    }

    @ApiOperation("根据id获取维度文件夹详情")
    @GetMapping("/getDimensionFolder/{id}")
    public ResultEntity<Object> getDimensionFolder(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionFolder(id));
    }

    @ApiOperation("修改维度文件夹")
    @PutMapping("/editDimensionFolder")
    public ResultEntity<Object> editDimensionFolder(@Validated @RequestBody DimensionFolderDTO dto) {
        return ResultEntityBuild.build(service.updateDimensionFolder(dto));
    }

    @ApiOperation("根据业务域id获取维度文件夹下相关维度信息")
    @GetMapping("/getDimensionFolderList/{businessAreaId}")
    public ResultEntity<Object> getDimensionFolderList(@PathVariable("businessAreaId") int businessAreaId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionFolderList(businessAreaId));
    }

    @ApiOperation("根据维度文件夹id集合,发布相关维度")
    @PostMapping("/publicDimensionFolder")
    public ResultEntity<Object> publicDimensionFolder(@Validated @RequestBody DimensionFolderPublishQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.batchPublishDimensionFolder(dto));
    }

    @ApiOperation("根据维度名称获取维度文件夹详情")
    @GetMapping("/getDimensionFolderByTableName")
    public ResultEntity<DimensionFolderDTO> getDimensionFolderByTableName(String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionFolderByTableName(tableName));
    }

}
