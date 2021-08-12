package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.ChartQueryObjectSsas;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.redis.RedisKeyBuild;
import com.fisk.common.redis.RedisKeyEnum;
import com.fisk.common.redis.RedisUtil;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
    @Resource
    RedisUtil redis;

    @ApiOperation("获取图表数据")
    @PostMapping("/get")
    public ResultEntity<DataServiceResult> get(@Validated @RequestBody ChartQueryObject query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, db.query(query));
    }

    @ApiOperation("下载图表数据")
    @GetMapping("/downLoad")
    public void downLoad(String key, HttpServletResponse response) {
        db.downLoad(key, response);
    }

    @ApiOperation("获取下载令牌")
    @PostMapping("/getDownLoadToken")
    public ResultEntity<String> downLoad(@RequestBody ChartQueryObject query) {
        String key = RedisKeyBuild.buildDownLoadToken();
        boolean res = redis.set(key, query, RedisKeyEnum.CHARTVISUAL_DOWNLOAD_TOKEN.getValue());
        return ResultEntityBuild.buildData(res ? ResultEnum.SUCCESS : ResultEnum.ERROR, key);
    }

    @ApiOperation("获取切片器数据")
    @PostMapping("/slicer")
    public ResultEntity<List<Map<String, Object>>> slicer(@Validated @RequestBody SlicerQueryObject query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, db.querySlicer(query));
    }

    @ApiOperation("获取数据")
    @PostMapping("/get_ssas")
    public ResultEntity<DataServiceResult> getSsas(@Validated @RequestBody ChartQueryObjectSsas query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, db.querySsas(query));
    }
}
