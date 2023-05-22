package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.access.TableHistoryDTO;
import com.fisk.mdm.service.ITableHistory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = { SwaggerConfig.TAG_15 })
@RestController
@RequestMapping("/tableHistory")
@Slf4j
public class TableHistoryController {

    @Resource
    ITableHistory service;

    @ApiOperation("获取表发布历史列表")
    @PostMapping("/getTableHistoryList")
    public ResultEntity<Object> getTableHistoryList(@RequestBody TableHistoryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableHistoryList(dto));
    }

    @ApiOperation("添加发布历史")
    @PostMapping("/addTableHistory")
    public ResultEntity<Object> addTableHistory(@Validated @RequestBody List<TableHistoryDTO> dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addTableHistory(dto));
    }

}
