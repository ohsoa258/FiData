package com.fisk.dataservice.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.service.ITableService;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/tableService")
@EnableAsync
public class TableServiceController {

    @Resource
    ITableService service;

    @ApiOperation("分页获取表服务数据")
    @PostMapping("/getTableServiceListData")
    public ResultEntity<Object> getTableServiceListData(@RequestBody TableServicePageQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableServiceListData(dto));
    }


}
