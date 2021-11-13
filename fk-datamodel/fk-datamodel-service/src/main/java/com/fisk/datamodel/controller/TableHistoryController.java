package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryQueryDTO;
import com.fisk.datamodel.service.ITableHistory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = { SwaggerConfig.TABLE_HISTORY })
@RestController
@RequestMapping("/tableHistory")
@Slf4j
public class TableHistoryController {

    @Resource
    ITableHistory service;

    @ApiOperation("获取表发布历史列表")
    @PostMapping("/getTableHistoryList")
    public ResultEntity<Object> getTableHistoryList(@RequestBody TableHistoryQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableHistoryList(dto));
    }

    @ApiOperation("添加发布历史")
    @PostMapping("/addTableHistory")
    public ResultEntity<Object> addTableHistory(@Validated @RequestBody TableHistoryDTO dto) {
        return ResultEntityBuild.build(service.addTableHistory(dto));
    }

}
