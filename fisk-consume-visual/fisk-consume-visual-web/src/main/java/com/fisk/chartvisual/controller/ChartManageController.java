package com.fisk.chartvisual.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.chartVisual.ChartPropertyDTO;
import com.fisk.chartvisual.dto.chartVisual.ChartPropertyEditDTO;
import com.fisk.chartvisual.dto.chartVisual.ReleaseChart;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.service.IChartManageService;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.chartvisual.dto.chartVisual.ChartQueryDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 可视化 1.0
 * 可视化接口已经过期,请使用可视化 2.0 版本
 * @author gy
 */
@Deprecated
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartManageController {

    @Resource
    IChartManageService service;

    @ApiOperation("添加草稿报表")
    @PostMapping("/addDraft")
    public ResultEntity<Object> addDraft(@Validated  @RequestBody ChartPropertyDTO dto) {
        return ResultEntityBuild.build(service.saveChartToDraft(dto));
    }

    @Deprecated
    @ApiOperation("添加报表")
    @PostMapping("/add")
    public ResultEntity<Long> addData(@Validated  @RequestBody ReleaseChart dto) {
        return service.saveChart(dto);
    }

    @Deprecated
    @ApiOperation("根据id获取报表")
    @GetMapping("/get")
    public ResultEntity<ChartPropertyVO> getDataById(int id, ChartQueryTypeEnum type) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataById(id, type));
    }

    @Deprecated
    @ApiOperation("修改报表")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody ChartPropertyEditDTO dto) {
        return ResultEntityBuild.build(service.updateChart(dto));
    }

    @Deprecated
    @ApiOperation("删除报表")
    @DeleteMapping("/delete")
    public ResultEntity<Object> deleteDataById(int id, ChartQueryTypeEnum type) {
        return ResultEntityBuild.build(service.deleteDataById(id, type));
    }

    @ApiOperation("获取用户权限的所有报表")
    @PostMapping("/page")
    public ResultEntity<Page<ChartPropertyVO>> listData(@RequestBody ChartQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query.page, query));
    }
}
