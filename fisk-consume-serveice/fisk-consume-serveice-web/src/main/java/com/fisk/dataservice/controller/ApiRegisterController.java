package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.standards.StandardsMenuDTO;
import com.fisk.datamanagement.dto.standards.StandardsSortDTO;
import com.fisk.datamanagement.dto.standards.StandardsTreeDTO;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.api.*;
import com.fisk.dataservice.dto.appserviceconfig.AppTableServiceConfigDTO;
import com.fisk.dataservice.service.IApiMenuConfigService;
import com.fisk.dataservice.service.IApiRegisterManageService;
import com.fisk.dataservice.vo.api.*;
import com.fisk.dataservice.vo.fileservice.FileServiceVO;
import com.fisk.dataservice.vo.tableapi.*;
import com.fisk.dataservice.vo.tableservice.TableServiceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description api注册控制器
 * @date 2022/1/6 14:51
 */

@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/apiRegister")
public class ApiRegisterController {
    @Resource
    private IApiRegisterManageService service;

    @Resource
    private IApiMenuConfigService apiMenuConfigService;

    @ApiOperation("分页查询所有api")
    @PostMapping("/page")
    public ResultEntity<Page<ApiConfigVO>> getAll(@RequestBody ApiRegisterQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("分页查询所有api订阅")
    @PostMapping("/getApiSubAll")
    public ResultEntity<PageDTO<ApiSubVO>> getApiSubAll(@RequestBody ApiSubQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getApiSubAll(dto));
    }

    @ApiOperation("分页查询所有表服务订阅")
    @PostMapping("/getTableServiceSubAll")
    public ResultEntity<PageDTO<TableServiceVO>> getTableServiceSubAll(@RequestBody ApiSubQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableServiceSubAll(dto));
    }

    @ApiOperation("分页查询所有文件订阅")
    @PostMapping("/getFileServiceSubAll")
    public ResultEntity<PageDTO<FileServiceVO>> getFileServiceSubAll(@RequestBody ApiSubQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFileServiceSubAll(dto));
    }

    @ApiOperation("添加api")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody ApiRegisterDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑api")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody ApiRegisterEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("编辑api基础信息")
    @PutMapping("/editApiData")
    public ResultEntity<Object> editApiData(@RequestBody ApiConfigEditDTO dto) {
        return ResultEntityBuild.build(service.editApiData(dto));
    }

    @ApiOperation("删除api")
    @DeleteMapping("/delete/{apiId}")
    public ResultEntity<Object> deleteData(@PathVariable("apiId") int apiId) {
        return ResultEntityBuild.build(service.deleteData(apiId));
    }

    @ApiOperation("查询api")
    @GetMapping("/detail/{apiId}")
    public ResultEntity<ApiRegisterDetailVO> detail(@PathVariable("apiId") int apiId) {
        return service.detail(apiId);
    }

    @ApiOperation("查询api字段列表")
    @GetMapping("/getFieldAll/{apiId}")
    public ResultEntity<List<FieldConfigVO>> getFieldAll(@PathVariable("apiId") int apiId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getFieldAll(apiId));
    }

    @ApiOperation("查询api字段加密列表")
    @GetMapping("/getFieldEncryptAll/{apiId}")
    public ResultEntity<FieldEncryptConfigDTO> getFieldEncryptAll(@PathVariable("apiId") int apiId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getFieldEncryptAll(apiId));
    }

    @ApiOperation("设置字段属性")
    @PutMapping("/setField")
    public ResultEntity<Object> setField(@RequestBody List<FieldConfigEditDTO> dto) {
        return ResultEntityBuild.build(service.setField(dto));
    }

    @ApiOperation("设置字段加密")
    @PutMapping("/setFieldEncrypt")
    public ResultEntity<Object> setFieldEncrypt(@RequestBody FieldEncryptConfigDTO dto) {
        return ResultEntityBuild.build(service.setFieldEncrypt(dto));
    }

    @ApiOperation("预览")
    @PostMapping("/preview")
    public ResultEntity<ApiPreviewVO> preview(@RequestBody ApiPreviewDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.preview(dto));
    }

    @ApiOperation("保存应用引入表服务配置")
    @PostMapping("/appTableServiceConfig")
    public ResultEntity<Object> appTableServiceConfig(@RequestBody List<AppTableServiceConfigDTO> dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.appTableServiceConfig(dto));
    }

    @ApiOperation("重点接口标记")
    @GetMapping("/importantOrUnimportant")
    public ResultEntity<List<String>> importantOrUnimportant(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(service.importantOrUnimportant(id));
    }

    @ApiOperation("获取数据消费(当天)")
    @GetMapping("/getConsumeServer")
    public ResultEntity<ConsumeServerVO> getConsumeServer() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getConsumeServer());
    }

    @ApiOperation("获取输出接口使用频率top5")
    @GetMapping("/getTopFrequency")
    public ResultEntity<List<TopFrequencyVO>> getTopFrequency() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getTopFrequency());
    }

    @ApiOperation("获取接口服务数量信息")
    @GetMapping("/getApiDataTotal")
    public ResultEntity<DataTotalVO> getApiDataTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getApiDataTotal());
    }
    @ApiOperation("获取表服务数量信息")
    @GetMapping("/getTableDataTotal")
    public ResultEntity<DataTotalVO> getTableDataTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getTableDataTotal());
    }

    @ApiOperation("获取api网关接口7天调用量")
    @GetMapping("/getApiTrafficLastWeek")
    public ResultEntity<List<ApiTrafficLastWeekVO>> getApiTrafficLastWeek() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getApiTrafficLastWeek());
    }

    @ApiOperation("获取api网关最近5次调用数据")
    @GetMapping("/getRecentApiGatewayCalls")
    public ResultEntity<List<RecentApiGatewayCallsVO>> getRecentApiGatewayCalls() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getRecentApiGatewayCalls());
    }
    @ApiOperation("查看api树形标签")
    @GetMapping("/getApiTree")
    public ResultEntity<List<ApiTreeDTO>> getApiTree(@RequestParam("serverType") Integer serverType) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, apiMenuConfigService.getApiTree(serverType));
    }

    @ApiOperation("添加或修改Api标签")
    @PostMapping("/addorUpdateApiMenu")
    public ResultEntity<Object> addorUpdateApiMenu(@RequestBody ApiMenuDTO dto) {
        return ResultEntityBuild.build(apiMenuConfigService.addorUpdateApiMenu(dto));
    }

    @ApiOperation("删除api标签")
    @PostMapping("/delApiMenu")
    public ResultEntity<Object> delApiMenu(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(apiMenuConfigService.delApiMenu(ids));
    }

    @ApiOperation("api排序更新")
    @PostMapping("/apiSort")
    public ResultEntity<Object> apiSort(@RequestBody ApiSortDTO dto) {
        return ResultEntityBuild.build(apiMenuConfigService.apiSort(dto));
    }

    @ApiOperation("获取所有标签")
    @PostMapping("/getAllTag")
    public ResultEntity<Object> getAllTag() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getAllTag());
    }
}
