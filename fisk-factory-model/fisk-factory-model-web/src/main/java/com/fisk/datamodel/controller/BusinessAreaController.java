package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.*;
import com.fisk.datamodel.service.IBusinessArea;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.BUSINESS_AREA})
@RestController
@RequestMapping("/business")
@Slf4j
public class BusinessAreaController {

    @Resource
    private IBusinessArea service;

    @PostMapping("/add")
    @ApiOperation(value = "添加业务域[对象]")
    public ResultEntity<Object> addData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.addData(businessAreaDTO));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<BusinessAreaDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "业务域修改(对象)")
    public ResultEntity<Object> editData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.updateBusinessArea(businessAreaDTO));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除业务域(url拼接)")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {
        return ResultEntityBuild.build(service.deleteBusinessArea(id));
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询(url拼接)")
    public ResultEntity<Page<Map<String, Object>>> queryByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "1") Integer rows) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.queryByPage(key, page, rows));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "获取业务域表字段")
    public ResultEntity<Object> getBusinessColumn(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getBusinessAreaColumn());
    }

    @PostMapping("/getDataList")
    @ApiOperation(value = "获取业务域数据列表")
    public ResultEntity<Page<BusinessPageResultDTO>> getDataList(@RequestBody BusinessQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataList(query));
    }

    @PostMapping("/getBusinessAreaPublicData")
    @ApiOperation(value = "根据事实字段集合,在Doris中创建相关表")
    public ResultEntity<Object> getBusinessAreaPublicData(@RequestBody IndicatorQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaPublicData(dto));
    }

    @PostMapping("/getBusinessAreaTable")
    @ApiOperation(value = "获取业务域下维度/事实表")
    public ResultEntity<Page<PipelineTableLogVO>> getBusinessAreaTable(@RequestBody PipelineTableQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaTable(dto));
    }

    @PostMapping("/getBusinessAreaTableDetail")
    @ApiOperation(value = "根据业务id、表类型、表id,获取表详情")
    public ResultEntity<BusinessAreaTableDetailDTO> getBusinessAreaTableDetail(@RequestBody BusinessAreaQueryTableDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaTableDetail(dto));
    }

    @PostMapping("/redirect")
    @ApiOperation(value = "跳转页面: 查询出当前表具体在哪个管道中使用,并给跳转页面提供数据")
    public ResultEntity<Object> redirect(@Validated @RequestBody ModelRedirectDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.redirect(dto));
    }
}
