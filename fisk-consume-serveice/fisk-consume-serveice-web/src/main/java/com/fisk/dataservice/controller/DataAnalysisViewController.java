package com.fisk.dataservice.controller;

import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataApplicationDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.dataanalysisview.*;
import com.fisk.dataservice.service.IDataViewService;
import com.fisk.dataservice.service.IDataViewThemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName: 数据分析视图服务
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@Api(tags = {SwaggerConfig.TAG_6})
@RestController
@RequestMapping("/dataAnalysisView")
public class DataAnalysisViewController {

    @Resource
    private IDataViewThemeService dataViewThemeService;

    @Resource
    private IDataViewService dataViewService;

    @ApiOperation("删除视图主题")
    @DeleteMapping("/removeViewTheme")
    public ResultEntity<Object> removeViewTheme(@RequestParam(value = "viewThemeId", defaultValue = "0") Integer viewThemeId){
        return ResultEntityBuild.build(dataViewThemeService.removeViewTheme(viewThemeId));
    }

    @ApiOperation("获取目标数据源列表")
    @GetMapping("/getTargetDbList")
    public ResultEntity<Object> getTargetDbList(){
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataViewThemeService.getTargetDbList());
    }

    @ApiOperation("新增视图主题")
    @PostMapping("/addViewTheme")
    public ResultEntity<Object> addViewTheme(@Validated @RequestBody DataViewThemeDTO dto){
        return ResultEntityBuild.build(dataViewThemeService.addViewTheme(dto));
    }

    @ApiOperation("修改视图主题")
    @PutMapping("/updateViewTheme")
    public ResultEntity<Object> updateViewTheme(@Validated @RequestBody DataViewThemeDTO dto){
        return ResultEntityBuild.build(dataViewThemeService.updateViewTheme(dto));
    }

    @ApiOperation("批量添加视图预览单表数据")
    @GetMapping("/getPreviewData")
    public ResultEntity<Object> getPreviewData(
            @RequestParam(value = "viewThemeId", defaultValue = "0") Integer viewThemeId,
            @RequestParam(value = "tableName", defaultValue = "") String tableName){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewService.getPreviewData(viewThemeId, tableName));
    }

    @ApiOperation("分页获取视图主题列表")
    @GetMapping("/getViewThemeList")
    public ResultEntity<PageDTO<DataViewThemeDTO>> getViewThemeList(
        @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(value = "pageSize", defaultValue = "8") Integer pageSize){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewThemeService.getViewThemeList(pageNum, pageSize));
    }

    @ApiOperation("获取视图主题下的数据视图列表")
    @GetMapping("/getViewList")
    public ResultEntity<PageDTO<DataViewDTO>> getViewList(
            @RequestParam(value = "viewThemeId", defaultValue = "0") Integer viewThemeId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewService.getViewList(viewThemeId, pageNum, pageSize));
    }

    @ApiOperation("获取视图主题目标数据源信息")
    @GetMapping("/getDataSourceByViewThemeId")
    public ResultEntity<Object> getDataSourceByViewThemeId(@RequestParam("viewThemeId") Integer viewThemeId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewThemeService.getDataSourceByViewThemeId(viewThemeId));
    }

    @ApiOperation("获取数据源表结构信息")
    @GetMapping("/getDataSourceMeta")
    public ResultEntity<Object> getDataSourceMeta(@RequestParam("viewThemeId") Integer viewThemeId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewService.getDataSourceMeta(viewThemeId));
    }

    @ApiOperation("获取数据源表字段结构信息")
    @GetMapping("/getSourceColumnMeta")
    public ResultEntity<Object> getSourceColumnMeta(
            @RequestParam(value = "viewThemeId", defaultValue = "0") Integer viewThemeId,
            @RequestParam(value = "tableName", defaultValue = "") String tableName,
            @RequestParam(value = "queryType", defaultValue = "1") Integer queryType){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewService.getSourceColumnMeta(viewThemeId, tableName, queryType));
    }

    @ApiOperation("获取数据源表字段结构信息")
    @PostMapping("/getDataAccessQueryList")
    public ResultEntity<Object> getDataAccessQueryList(@Validated @RequestBody SelSqlResultDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewService.getDataAccessQueryList(dto));
    }

    @ApiOperation("添加数据视图")
    @PostMapping("/addDataView")
    public ResultEntity<Object> addDataView(@Validated @RequestBody SaveDataViewDTO dto){
        return ResultEntityBuild.build(dataViewService.addDataView(dto));
    }

    @ApiOperation("删除数据视图")
    @DeleteMapping("/removeDataView")
    public ResultEntity<Object> removeDataView(
            @RequestParam(value = "viewId", defaultValue = "0") Integer viewId){
        return ResultEntityBuild.build(dataViewService.removeDataView(viewId));
    }

    @ApiOperation("修改数据视图名称")
    @PutMapping("/editDataView")
    public ResultEntity<Object> editDataView(@Validated @RequestBody EditDataViewDTO dto){
        return ResultEntityBuild.build(dataViewService.editDataView(dto));
    }

    @ApiOperation("获取数据视图字段信息")
    @GetMapping("/getViewTable")
    public ResultEntity<Object> getViewTableFields(
            @RequestParam(value = "viewId", defaultValue = "0") Integer viewId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewService.getViewTableFields(viewId));
    }

    @ApiOperation("修改数据视图及执行脚本内容")
    @PutMapping("/updateDataView")
    public ResultEntity<Object> updateDataView(@Validated @RequestBody UpdateDataViewDTO dto){
        return ResultEntityBuild.build(dataViewService.updateDataView(dto));
    }

    @ApiOperation("批量添加数据视图")
    @PostMapping("/addBatchDataView")
    public ResultEntity<Object> addBatchDataView(@Validated @RequestBody SaveBatchDataViewDTO dto){
        return ResultEntityBuild.build(dataViewService.addBatchDataView(dto));
    }


    @ApiOperation("获取视图服务据应用信息元数")
    @GetMapping("/getViewServiceMetaData")
    public ResultEntity<List<MetaDataEntityDTO>> getViewServiceMetaData(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,dataViewThemeService.getViewServiceMetaData());
    }

    @ApiOperation("获取View服务的所有应用")
    @GetMapping("/getViewService")
    public ResultEntity<List<AppBusinessInfoDTO>> getViewService() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, dataViewThemeService.getViewService());
    }

}
