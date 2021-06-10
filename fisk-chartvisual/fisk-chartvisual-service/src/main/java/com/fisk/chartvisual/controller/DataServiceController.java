package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 数据服务
 *
 * @author gy
 */
@RestController
@RequestMapping("/data")
@Slf4j
public class DataServiceController {

    @Resource
    IDataService db;

    @ApiOperation("获取图表数据")
    @PostMapping("/get")
    public ResultEntity<List<Map<String, Object>>> get(@Validated @RequestBody ChartQueryObject query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, db.query(query));
    }

    @ApiOperation("获取切片器数据")
    @PostMapping("/slicer")
    public ResultEntity<List<Map<String, Object>>> slicer(@Validated @RequestBody SlicerQueryObject query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, db.querySlicer(query));
    }
}
