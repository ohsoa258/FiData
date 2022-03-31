package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.service.IChartManageService;
import com.fisk.common.core.response.ResultEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2021/12/9 20:26
 */
@RestController
@RequestMapping("/systemWebIndex")
public class SystemWebIndexController {

    @Resource
    IChartManageService service;

    @ApiOperation("获取报表数量")
    @GetMapping("/getReports")
    public ResultEntity<Long> getReports() {
        return service.amount();
    }
}
