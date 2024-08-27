package com.fisk.datagovernance.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.DataSourceInfoDTO;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.service.dataquality.DatacheckCodeService;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.service.dataquality.IDatacheckStandardsGroupService;
import com.fisk.datagovernance.vo.dataquality.datacheck.*;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 校验规则
 * @date 2022/3/22 16:17
 */
@Api(tags = {SwaggerConfig.DATA_CHECK_CONTROLLER})
@RestController
@RequestMapping("/datacheck")
public class DataCheckController {
    @Resource
    private IDataCheckManageService service;

    @Resource
    private IDatacheckStandardsGroupService datacheckStandardsGroupService;
    @Resource
    private DatacheckCodeService datacheckCodeService;

    @ApiOperation("获取规则搜索条件")
    @GetMapping("/getRuleSearchWhere")
    public ResultEntity<DataCheckRuleSearchWhereVO> getRuleSearchWhere() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.getRuleSearchWhere());
    }

    @ApiOperation("查询全部校验规则")
    @PostMapping("/getAllRule")
    public ResultEntity<PageDTO<DataCheckVO>> getAllRule(@RequestBody DataCheckQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllRule(dto));
    }

    @ApiOperation("添加数据校验模板组件")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody DataCheckDTO dto) {
        return ResultEntityBuild.build(service.addData(dto, true));
    }

    @ApiOperation("编辑数据校验模板组件")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody DataCheckEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto, true));
    }

    @ApiOperation("获取关联值")
    @GetMapping("/getCheckCodeList")
    public ResultEntity<Object> getCheckCodeList() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, datacheckCodeService.getCheckCodeList());
    }

    @ApiOperation("删除数据校验模板组件")
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }

    @ApiOperation("接口验证（同步前）")
    @PostMapping("/interfaceCheckData")
    public ResultEntity<List<DataCheckResultVO>> interfaceCheckData(@Validated @RequestBody DataCheckWebDTO dto) {
        return service.interfaceCheckData(dto);
    }

    @ApiOperation("NIFI同步验证（同步中）")
    @PostMapping("/syncCheckData")
    public ResultEntity<List<DataCheckResultVO>> syncCheckData(@Validated @RequestBody DataCheckSyncDTO dto) {
        return service.nifiSyncCheckData(dto);
    }

    @ApiOperation("获取数据检查结果日志搜索条件")
    @GetMapping("/getDataCheckLogSearchWhere")
    public ResultEntity<DataCheckRuleSearchWhereVO> getDataCheckLogSearchWhere() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataCheckLogSearchWhere());
    }

    @ApiOperation("获取数据检查结果日志分页列表")
    @PostMapping("/getDataCheckLogsPage")
    public ResultEntity<Page<DataCheckLogsVO>> getDataCheckLogsPage(@RequestBody DataCheckLogsQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataCheckLogsPage(dto));
    }

    @ApiOperation("根据日志Id查询数据检查结果")
    @PostMapping("/getDataCheckLogsResult")
    public ResultEntity<JSONArray> getDataCheckLogsResult(@RequestParam("logId") long logId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataCheckLogsResult(logId));
    }

    @ApiOperation("根据检查规则Id删除数据检查日志")
    @PostMapping("/deleteDataCheckLogs")
    public ResultEnum deleteDataCheckLogs(@RequestParam("ruleId") long ruleId) {
        return service.deleteDataCheckLogs(ruleId);
    }

    @ApiOperation("检查规则日志增加质量分析")
    @PostMapping("/dataCheckLogAddQualityAnalysis")
    public ResultEntity<ResultEnum> dataCheckLogAddQualityAnalysis(@RequestBody DataCheckLogCommentDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.dataCheckLogAddQualityAnalysis(dto));

    }

    @ApiOperation("生成数据检查结果Excel")
    @PostMapping("/createDataCheckResultExcel")
    public ResultEntity<String> createDataCheckResultExcel(@RequestParam("logIds") String logIds) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.createDataCheckResultExcel(logIds));
    }

    @ApiOperation("删除数据检查结果")
    @DeleteMapping("/deleteCheckResult")
    public ResultEntity<Object> deleteCheckResult() {
        return ResultEntityBuild.build(service.deleteCheckResult());
    }


    @ApiOperation("获取数据校验数据元标准组中数据")
    @GetMapping("/getDataCheckStandardsGroup")
    public ResultEntity<PageDTO<DatacheckStandardsGroupVO>> getDataCheckStandardsGroup(@RequestParam("standardsId") Integer standardsId,
                                                                                       @RequestParam("current") Integer current,
                                                                                       @RequestParam("size") Integer size) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, datacheckStandardsGroupService.getDataCheckStandardsGroup(standardsId, current, size));
    }

    @ApiOperation("添加数据校验数据元标准组")
    @PostMapping("/addDataCheckStandardsGroup")
    public ResultEntity<Object> addDataCheckStandardsGroup(@RequestBody DatacheckStandardsGroupDTO dto) {
        ResultEnum resultEnum = datacheckStandardsGroupService.addDataCheckStandardsGroup(dto);
        return ResultEntityBuild.buildData(resultEnum, resultEnum);
    }

    @ApiOperation("修改数据校验数据元标准组")
    @PostMapping("/editDataCheckStandardsGroup")
    public ResultEntity<Object> editDataCheckStandardsGroup(@RequestBody DatacheckStandardsGroupDTO dto) {
        ResultEnum resultEnum = datacheckStandardsGroupService.editDataCheckStandardsGroup(dto);
        return ResultEntityBuild.buildData(resultEnum, resultEnum);
    }

    @ApiOperation("删除数据校验数据元标准组")
    @GetMapping("/deleteDataCheckStandardsGroup")
    public ResultEntity<Object> deleteDataCheckStandardsGroup(@RequestParam("id") Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, datacheckStandardsGroupService.deleteDataCheckStandardsGroup(id));
    }

    @ApiOperation("根据数据元目录ID获取数据元校验规则组")
    @PostMapping("/getRuleGroupByStandardMenuIds")
    public ResultEntity<List<DataCheckRuleGroupVO>> getRuleGroupByStandardMenuIds(@RequestBody DataCheckRuleGroupDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, datacheckStandardsGroupService.getRuleGroupByStandardMenuIds(dto));
    }

    @ApiOperation("查看数据源结构树")
    @GetMapping("/getDataSourceTree")
    public ResultEntity<List<DataSourceInfoDTO>> getDataSourceTree(@RequestParam("dbId") Integer dbId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataSourceTree(dbId));
    }

    @ApiOperation("获取表字段信息")
    @PostMapping("/getColumn")
    public ResultEntity<Object> getColumn(@RequestBody ColumnQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn(dto));
    }

    @ApiOperation("获取所有数据校验规则数量")
    @GetMapping("/getDataCheckRoleTotal")
    public ResultEntity<Object> getDataCheckRoleTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataCheckRoleTotal());
    }

    @ApiOperation("修改数据校验数据元标准组(数据元更新同步)")
    @PostMapping("/editDataCheckByStandards")
    public ResultEntity<Object> editDataCheckByStandards(@RequestBody StandardsDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, datacheckStandardsGroupService.editDataCheckByStandards(dto));
    }
}
