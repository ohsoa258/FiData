package com.fisk.chartvisual.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import com.fisk.chartvisual.service.IChartManage;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

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
    IChartManage service;

    @PostMapping("/addDraft")
    public ResultEntity<Object> addDraft(@Validated @RequestBody ChartPropertyDTO dto) {
        return ResultEntityBuild.build(service.saveChartToDraft(dto));
    }

    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody ReleaseChart dto) {
        return ResultEntityBuild.build(service.saveChart(dto));
    }

    @GetMapping("/get")
    public ResultEntity<ChartPropertyVO> getDataById(int id, ChartQueryTypeEnum type) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataById(id, type));
    }

    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody ChartPropertyEditDTO dto) {
        return ResultEntityBuild.build(service.updateChart(dto));
    }

    @DeleteMapping("/delete")
    public ResultEntity<Object> deleteDataById(int id, ChartQueryTypeEnum type) {
        return ResultEntityBuild.build(service.deleteDataById(id, type));
    }

    @GetMapping("/page")
    public ResultEntity<Page<ChartPropertyVO>> listData(Page<ChartPropertyVO> page, ChartQuery query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(page, query));
    }
}
