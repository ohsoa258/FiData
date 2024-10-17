package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterEditDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.AppRegisterQueryDTO;
import com.fisk.datagovernance.service.dataquality.DatacheckServerAppConfigService;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppRegisterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: wangjian
 * @Date: 2024-10-14
 * @Description:
 */
@Api(tags = {SwaggerConfig.DATA_CHECK_SERVER_CONTROLLER})
@RestController
@RequestMapping("/datacheckserver")
public class DataCheckServerController {

    @Resource
    DatacheckServerAppConfigService service;

    @ApiOperation(value = "筛选器")
    @PostMapping("/pageFilter")
    public ResultEntity<Page<AppRegisterVO>> pageFilter(@RequestBody AppRegisterQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.pageFilter(dto));
    }

    @ApiOperation("添加应用")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody AppRegisterDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑应用")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody AppRegisterEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("删除应用")
    @DeleteMapping("/delete/{appId}")
    public ResultEntity<Object> deleteData(@PathVariable("appId") int appId) {
        return ResultEntityBuild.build(service.deleteData(appId));
    }

    @ApiOperation(value = "应用过滤字段")
    @GetMapping("/getColumn")
    public ResultEntity<Object> getBusinessColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn());
    }
}
