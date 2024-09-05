package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.fact.FactTransDTO;
import com.fisk.datamodel.dto.fact.FactTreeDTO;
import com.fisk.datamodel.dto.fact.ModelSyncDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.service.IFact;
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
@Api(tags = {SwaggerConfig.FACT})
@RestController
@RequestMapping("/fact")
@Slf4j
public class FactController {
    @Resource
    IFact service;

    @ApiOperation("获取事实表列表")
    @PostMapping("/getFactList")
    public ResultEntity<Object> getFactList(@RequestBody QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactList(dto));
    }

    @ApiOperation("添加事实表")
    @PostMapping("/addFact")
    public ResultEntity<Object> addFact(@Validated @RequestBody FactDTO dto) {
        return ResultEntityBuild.build(service.addFact(dto));
    }

    @ApiOperation("根据id获取事实表详情")
    @GetMapping("/getFact/{id}")
    public ResultEntity<Object> getFact(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFact(id));
    }

    @ApiOperation("修改事实表")
    @PutMapping("/editFact")
    public ResultEntity<Object> editFact(@Validated @RequestBody FactDTO dto) {
        return ResultEntityBuild.build(service.updateFact(dto));
    }

    @ApiOperation("删除事实表")
    @DeleteMapping("/deleteFact/{id}")
    public ResultEntity<Object> deleteFact(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteFact(id));
    }

    @ApiOperation("删除事实表 删除前检查是否在管道中")
    @DeleteMapping("/deleteFactByCheck/{id}")
    public ResultEntity<Object> deleteFactByCheck(@PathVariable("id") int id) {
        return service.deleteFactByCheck(id);
    }

    @ApiOperation("获取事实表以及事实字段表数据")
    @GetMapping("/getFactDropList")
    public ResultEntity<Object> getFactDropList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactDropList());
    }

    @ApiOperation("获取事实表下拉列表")
    @GetMapping("/getFactScreenDropList")
    public ResultEntity<Object> getFactScreenDropList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactScreenDropList());
    }

    @ApiOperation("修改事实sql脚本")
    @PutMapping("/editFactSql")
    public ResultEntity<Object> editFactSql(@Validated @RequestBody DimensionSqlDTO dto) {
        return ResultEntityBuild.build(service.updateFactSql(dto));
    }

    @ApiOperation("修改事实发布状态")
    @PutMapping("/updateFactPublishStatus")
    public void updateFactPublishStatus(@RequestBody ModelPublishStatusDTO dto) {
        service.updateFactPublishStatus(dto);
    }

    @ApiOperation("获取发布成功所有事实表")
    @GetMapping("/getPublishSuccessFactTable/{businessId}")
    public ResultEntity<Object> getPublishSuccessFactTable(@PathVariable("businessId") int businessId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getPublishSuccessFactTable(businessId));
    }

    /**
     * 获取事实tree
     *
     * @return
     */
    @ApiOperation("获取事实tree")
    @GetMapping("/getFactTree")
    public List<FactTreeDTO> getFactTree() {
        return service.getFactTree();
    }

    /**
     * 事实表跨业务域移动
     *
     * @param dto
     * @return
     */
    @ApiOperation("事实表跨业务域移动")
    @PutMapping("/transFactToBArea")
    public ResultEntity<Object> transFactToBArea(@RequestBody FactTransDTO dto) {
        return service.transFactToBArea(dto);
    }


    /**
     * 数仓建模 同步表数据（触发nifi）
     *
     * @param dto
     * @return
     */
    @PostMapping("/modelSyncData")
    @ApiOperation(value = "数仓建模 同步表数据（触发nifi）")
    public ResultEntity<Object> modelSyncData(@RequestBody ModelSyncDataDTO dto) {
        return service.modelSyncData(dto);
    }


    @PostMapping("/getFactTableByIds")
    @ApiOperation("根据业务id集合获取业务详情")
    public ResultEntity<List<FactDTO>> getFactTableByIds(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactTableByIds(ids));
    }
}
