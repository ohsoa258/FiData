package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.ChartPropertyDTO;
import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.service.IChartManage;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

}
