package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.atvserviceanalyse.AtvServiceMonitoringQueryDTO;
import com.fisk.dataservice.service.IATVServiceAnalyseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 11:17
 * @description Api && Table && View 服务数据分析 Controller
 */
@Api(tags = {SwaggerConfig.TAG_8})
@RestController
@RequestMapping("/ServiceAnalyse")
public class ATVServiceAnalyseController {
    @Resource
    private IATVServiceAnalyseService analyseService;

    @ApiOperation(value = "Api && Table && View服务数据报表分析")
    @GetMapping("/getServiceanalysis")
    public ResultEntity<Object> getServiceanalysis() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getServiceAnalyse());
    }

    @ApiOperation(value = "统计数据服务API熔断情况")
    @GetMapping("/getAtvCallApiFuSingAnalyse")
    public ResultEntity<Object> getAtvCallApiFuSingAnalyse() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getAtvCallApiFuSingAnalyse());
    }

    @ApiOperation(value = "统计数据服务API昨天和今天调用情况")
    @GetMapping("/getAtvYasCallApiAnalyse")
    public ResultEntity<Object> getAtvYasCallApiAnalyse() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getAtvYasCallApiAnalyse());
    }

    @ApiOperation(value = "统计数据服务API今天调用情况前20条")
    @GetMapping("/getAtvTopCallApiAnalyse")
    public ResultEntity<Object> getAtvTopCallApiAnalyse() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getAtvTopCallApiAnalyse());
    }

    @ApiOperation(value = "扫描数据服务API是否熔断")
    @GetMapping("/scanDataServiceApiIsFuSing")
    public ResultEntity<Object> scanDataServiceApiIsFuSing() {
        analyseService.scanDataServiceApiIsFuSing();
        return ResultEntityBuild.build(ResultEnum.SUCCESS, true);
    }

    @ApiOperation(value = "服务监控应用以及API下拉框数据")
    @PostMapping("/getAtvServiceDropdownCard")
    public ResultEntity<Object> getAtvServiceDropdownCard(@RequestBody AtvServiceMonitoringQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getAtvServiceDropdownCard(dto));
    }

    @ApiOperation(value = "API当天调用时长TOP20：接口调用耗时排名")
    @PostMapping("/getAtvApiTimeConsumingRanking")
    public ResultEntity<Object> getAtvApiTimeConsumingRanking(@RequestBody AtvServiceMonitoringQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getAtvApiTimeConsumingRanking(dto));
    }

    @ApiOperation(value = "API当天调用趋势：接口调用成功失败的数量排名")
    @PostMapping("/getAtvApiSuccessFailureRanking")
    public ResultEntity<Object> getAtvApiSuccessFailureRanking(@RequestBody AtvServiceMonitoringQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getAtvApiSuccessFailureRanking(dto));
    }

    @ApiOperation(value = "API申请人明细：负责人创建的应用下绑定的API")
    @PostMapping("/getAtvApiPrincipalDetailAppBindApi")
    public ResultEntity<Object> getAtvApiPrincipalDetailAppBindApi(@RequestBody AtvServiceMonitoringQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getAtvApiPrincipalDetailAppBindApi(dto));
    }

    @ApiOperation(value = "API申请次数TOP20：API所绑定的应用排名")
    @PostMapping("/getAtvApiSqCountApiBindAppRanking")
    public ResultEntity<Object> getAtvApiSqCountApiBindAppRanking(@RequestBody AtvServiceMonitoringQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, analyseService.getAtvApiSqCountApiBindAppRanking(dto));
    }
}
