package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.tablehistory.TableHistoryDTO;
import com.fisk.datamodel.dto.tablehistory.TableHistoryQueryDTO;
import com.fisk.datamodel.service.ITableHistory;
import com.fisk.task.dto.DwLogResultDTO;
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
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.TABLE_HISTORY})
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
    public ResultEntity<Object> addTableHistory(@Validated @RequestBody List<TableHistoryDTO> dto) {
        return ResultEntityBuild.build(service.addTableHistory(dto));
    }

    /**
     * 获取数仓表单表发布时，nifi的同步情况：日志+报错信息
     *
     * @param dto
     * @return
     */
    @ApiOperation("获取数仓表单表发布时，nifi的同步情况：日志+报错信息")
    @PostMapping("/getDwPublishNifiStatus")
    public ResultEntity<DwLogResultDTO> getDwPublishNifiStatus(@RequestBody TableHistoryQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDwPublishNifiStatus(dto));
    }

}
