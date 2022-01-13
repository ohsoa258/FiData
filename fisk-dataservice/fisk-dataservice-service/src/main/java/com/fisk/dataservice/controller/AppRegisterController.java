package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.app.*;
import com.fisk.dataservice.vo.app.*;
import com.fisk.dataservice.service.IAppRegisterManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 应用注册控制器
 * @date 2022/1/6 14:51
 */

@Api(tags = {SwaggerConfig.TAG_2})
@RestController
@RequestMapping("/appRegister")
public class AppRegisterController {
    @Resource
    private IAppRegisterManageService service;

    @ApiOperation(value = "获取下游系统过滤字段")
    @GetMapping("/getColumn")
    public ResultEntity<Object> getBusinessColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn());
    }

    @ApiOperation(value = "筛选器")
    @PostMapping("/pageFilter")
    public ResultEntity<Page<AppRegisterVO>> pageFilter(@RequestBody AppRegisterQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.pageFilter(dto));
    }

    @ApiOperation("分页查询所有应用")
    @GetMapping("/page")
    public ResultEntity<Page<AppRegisterVO>> getAll(Page<AppRegisterVO> dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("添加用户")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody AppRegisterDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑用户")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody AppRegisterEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/delete")
    public ResultEntity<Object> deleteData(@PathVariable("appId") Integer appId) {
        return ResultEntityBuild.build(service.deleteData(appId));
    }

    @ApiOperation("分页查询应用API订阅")
    @GetMapping("/appApiPage")
    public ResultEntity<Page<AppApiSubVO>> getSubscribeAll(Page<AppApiSubVO> page,AppApiSubQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSubscribeAll(page,dto));
    }

    @ApiOperation("应用订阅api")
    @PostMapping("/appSubscribe")
    public ResultEntity<Object> appSubscribe(@Validated @RequestBody AppApiSubDTO dto) {
        return ResultEntityBuild.build(service.appSubscribe(dto));
    }

    @ApiOperation("重置密码")
    @PutMapping("/resetPwd")
    public ResultEntity<Object> resetPwd(@Validated @RequestBody AppPwdResetDTO dto) {
        return ResultEntityBuild.build(service.resetPwd(dto));
    }

    @ApiOperation("生成文档")
    @PostMapping("/createDoc")
    public ResultEntity<Object> createDoc(@PathVariable("id") Integer appId) {
        return ResultEntityBuild.build(service.createDoc(appId));
    }

    @ApiOperation("查询参数")
    @GetMapping("/getParmAll")
    public ResultEntity<List<AppApiParmVO>> getParmAll(AppApiParmQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getParmAll(dto));
    }

    @ApiOperation("设置内置参数")
    @GetMapping("/setParm")
    public ResultEntity<Object> setParm(AppApiBuiltinParmEditDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setParm(dto));
    }
}
