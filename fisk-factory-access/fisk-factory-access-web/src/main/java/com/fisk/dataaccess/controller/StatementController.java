package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.service.IDataSourceReport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-21 11:19
 * @description
 */
@Api(tags = {SwaggerConfig.STATEMENT})
@RestController
@RequestMapping("/Statement")
public class StatementController {
    @Resource
    IDataSourceReport iDataSourceReport;

    @ApiOperation("获取数据来源报表信息")
    @GetMapping("/getDataSourceReport")
    public ResultEntity<Object> getDataSourceReport(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iDataSourceReport.getDataSourceReportDTO());
    }
}
