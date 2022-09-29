package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.service.ISavepointHistory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = SwaggerConfig.SAVEPOINT_HISTORY)
@RestController
@RequestMapping("/SavepointHistory")
public class SavepointHistoryController {

    @Resource
    ISavepointHistory service;

    @GetMapping("/getList/{tableAccessId}")
    @ApiOperation(value = "回显: 根据id查询数据")
    public ResultEntity<Object> getData(@PathVariable("tableAccessId") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSavepointHistory(id));
    }

}
