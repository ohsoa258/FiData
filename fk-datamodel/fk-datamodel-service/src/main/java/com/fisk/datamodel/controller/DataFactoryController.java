package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.service.IDataFactory;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATAFACTORY})
@RestController
@RequestMapping("/DataFactory")
public class DataFactoryController {
    @Resource
    IDataFactory service;

    @GetMapping("/getTableId")
    public ResultEntity<Object> getTableId(NifiComponentsDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableIds(dto));
    }

}
