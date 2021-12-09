package com.fisk.chartvisual.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.service.IChartManageService;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.chartvisual.dto.ChartQueryDTO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图表管理
 *
 * @author gy
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartManageController {

    @Resource
    IChartManageService service;

    @ApiOperation("添加草稿报表")
    @PostMapping("/addDraft")
    public ResultEntity<Object> addDraft(@Validated  @RequestBody  ChartPropertyDTO dto) {
        return ResultEntityBuild.build(service.saveChartToDraft(dto));
    }


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

    @ApiOperation("获取用户权限的所有报表")
    @PostMapping("/page")
    public ResultEntity<Page<ChartPropertyVO>> listData(@RequestBody ChartQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query.page, query));
    }

    @ApiOperation("获取报表数据可视化数量")
    @GetMapping("/amount")
    public ResultEntity<Long> amount() {
        return service.amount();
    }
}
