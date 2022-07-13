package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.service.IGlobalSearch;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.GLOBAL_SEARCH})
@RestController
@RequestMapping("/GlobalSearch")
public class GlobalSearchController {

    @Resource
    IGlobalSearch service;

    @ApiOperation("首页文本框查询Entity")
    @GetMapping("/searchQuick")
    public ResultEntity<Object> searchQuick(String query, int limit, int offset) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.searchQuick(query, limit, offset));
    }

    @ApiOperation("首页文本框查询Suggestions")
    @GetMapping("/searchSuggestions")
    public ResultEntity<Object> searchSuggestions(String prefixString) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.searchSuggestions(prefixString));
    }

    @ApiOperation("全局搜索-模糊查询业务分类")
    @GetMapping("/searchClassification")
    public ResultEntity<Object> searchClassification(String keyword) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.searchClassification(keyword));
    }

}
