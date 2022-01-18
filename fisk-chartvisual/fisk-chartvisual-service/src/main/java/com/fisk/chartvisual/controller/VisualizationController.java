package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.DataSourceDTO;
import com.fisk.chartvisual.service.VisualizationService;
import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2022/1/10 16:26
 */
@RestController
@RequestMapping("/visual")
@Slf4j
public class VisualizationController {

    @Resource
    VisualizationService visualizationService;

    @ApiOperation("可视化获取图表数据")
    @PostMapping("/getData")
    public ResultEntity<DataServiceResult> getData(@Validated @RequestBody ChartQueryObjectVO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, visualizationService.buildSql(query));
    }

    @ApiOperation("图片上传到服务器")
    @PostMapping("/upload")
    @ResponseBody
    public ResultEntity<String> upload(@RequestParam("file") MultipartFile file) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, visualizationService.upload(file));
    }

    @ApiOperation("可视化获取数据源")
    @PostMapping("/getDataDomain")
    public ResultEntity<Object> getData(@RequestBody DataSourceDTO dto) {
        return visualizationService.listDataDomain(dto);
    }
}
