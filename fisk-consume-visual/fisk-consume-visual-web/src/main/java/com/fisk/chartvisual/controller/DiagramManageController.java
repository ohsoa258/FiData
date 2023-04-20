package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.config.SwaggerConfig;
import com.fisk.chartvisual.dto.chartvisual.ChartPropertyEditDTO;
import com.fisk.chartvisual.dto.chartvisual.ReleaseChart;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.service.DiagramManageService;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Version 2.0 图表管理
 * @author WangYan
 * @date 2022/2/21 11:27
 */
@Api(tags = {SwaggerConfig.TAG_6})
@RestController
@RequestMapping("/diagram")
@Slf4j
public class DiagramManageController {

    @Resource
    DiagramManageService service;

    @ApiOperation("添加报表")
    @PostMapping("/add")
    public ResultEntity<Long> addData(@Validated  @RequestBody  ReleaseChart dto) {
        return service.saveChart(dto);
    }

    @ApiOperation("根据id获取报表")
    @GetMapping("/get")
    public ResultEntity<ChartPropertyVO> getDataById(int id, ChartQueryTypeEnum type) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataById(id, type));
    }

    @ApiOperation("修改报表")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody ChartPropertyEditDTO dto) {
        return ResultEntityBuild.build(service.updateChart(dto));
    }

    @ApiOperation("删除报表")
    @DeleteMapping("/delete")
    public ResultEntity<Object> deleteDataById(int id, ChartQueryTypeEnum type) {
        return ResultEntityBuild.build(service.deleteDataById(id, type));
    }
}
