package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeAddDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;
import com.fisk.datamodel.service.IFactAttribute;
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
@Api(tags = { SwaggerConfig.FACT_ATTRIBUTE })
@RestController
@RequestMapping("/factAttribute")
@Slf4j
public class FactAttributeController {

    @Resource
    IFactAttribute service;

    /*@ApiOperation("获取事实字段表列表")
    @GetMapping("/getFactAttributeList/{factId}")
    public ResultEntity<Object> getFactAttributeList(@PathVariable int factId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeList(factId));
    }*/

    @ApiOperation("获取事实字段表列表")
    @GetMapping("/getFactAttributeList/{id}")
    public ResultEntity<Object> getFactAttributeList(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeDataList(id));
    }

    @ApiOperation("添事实字段")
    @PostMapping("/addFactAttribute")
    public ResultEntity<Object> addFactAttribute(@Validated @RequestBody FactAttributeAddDTO dto)
    {
        return ResultEntityBuild.build(service.addFactAttribute(dto.factId,dto.isPublish,dto.list));
    }

    @ApiOperation("删除事实字段")
    @PostMapping("/deleteFactAttribute")
    public ResultEntity<Object> deleteFactAttribute(@RequestBody List<Integer> ids)
    {
        return ResultEntityBuild.build(service.deleteFactAttribute(ids));
    }

    @GetMapping("/getFactAttributeDetail/{id}")
    @ApiOperation("根据事实字段id获取事实字段详情")
    public ResultEntity<Object> getFactAttributeDetail(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeDetail(id));
    }

    @ApiOperation("修改事实字段")
    @PutMapping("/editFactAttribute")
    public ResultEntity<Object> editFactAttribute(@Validated @RequestBody FactAttributeUpdateDTO dto) {
        return ResultEntityBuild.build(service.updateFactAttribute(dto));
    }

    @GetMapping("/getFactEntity")
    @ApiOperation("获取事实表元数据(用于Doris创建表)")
    public ResultEntity<ModelMetaDataDTO> getFactEntity(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactMetaData(id));
    }

    @GetMapping("/getFactAttributeData")
    @ApiOperation("根据事实id获取事实字段详情")
    public ResultEntity<Object> getFactAttributeData(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.GetFactAttributeData(id));
    }

    @ApiOperation("根据事实id获取事实字段所有来源id")
    @GetMapping("/getFactAttributeSourceId/{id}")
    public ResultEntity<Object> getFactAttributeSourceId(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeSourceId(id));
    }

}
