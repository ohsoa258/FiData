package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.service.VisualizationService;
import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2022/1/10 16:26
 */
@RestController
@RequestMapping("/visualization")
@Slf4j
public class VisualizationController {

    @Resource
    VisualizationService visualizationService;

    @ApiOperation("可视化生成Sql")
    @PostMapping("/buildSql")
    public ResultEntity<DataServiceResult> get(@Validated @RequestBody ChartQueryObjectVO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, visualizationService.buildSql(query));
    }
}
