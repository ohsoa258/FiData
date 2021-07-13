package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.service.ApiFieldService;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/13 13:26
 */
@RestController
@RequestMapping("/api")
public class ApiFieldController {

    @Resource
    private ApiFieldService configureFieldService;

    @ApiOperation("根据路径查询")
    @RequestMapping("/query/*")
    public ResultEntity<List<Object>> queryData(String apiRoute, Integer offset, Integer limit) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,configureFieldService.queryField(apiRoute,offset,limit));
    }
}
