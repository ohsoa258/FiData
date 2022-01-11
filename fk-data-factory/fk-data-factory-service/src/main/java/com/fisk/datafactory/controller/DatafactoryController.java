package com.fisk.datafactory.controller;

import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.service.IDataFactory;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public boolean loadDepend(@RequestBody LoadDependDTO dto) {

        return service.loadDepend(dto);
    }

}
