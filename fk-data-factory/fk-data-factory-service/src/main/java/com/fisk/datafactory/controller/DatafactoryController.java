package com.fisk.datafactory.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.service.IDataFactory;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 * @version 1.3
 * @description 对外提供的feign接口API
 * @date 2022/1/11 11:51
 */
@RestController
@RequestMapping("/dataFactory")
public class DatafactoryController {

    @Resource
    private IDataFactory service;

    @PostMapping("/loadDepend")
    @ApiOperation(value = "判断物理表是否在管道使用")
    public ResultEntity<Boolean> loadDepend(@RequestBody LoadDependDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.loadDepend(dto));
    }

}
