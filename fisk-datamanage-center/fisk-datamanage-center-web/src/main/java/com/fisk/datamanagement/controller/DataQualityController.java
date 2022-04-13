package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.service.IDataQuality;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATA_QUALITY})
@RestController
@RequestMapping("/DataQuality")
public class DataQualityController {

    @Resource
    IDataQuality service;

    @ApiOperation("根据配置文件索引id,获取数据源配置信息")
    @GetMapping("/getDataSourceConfig/{index}")
    public ResultEntity<Object> getDataSourceConfig(@PathVariable("index") int index) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataSourceConfig(index));
    }

}
