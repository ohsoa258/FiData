package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.complextype.GeographyDTO;
import com.fisk.mdm.service.IComplexType;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/complexType")
public class ComplexTypeController {

    @Resource
    private IComplexType service;

    @ApiOperation("新增经纬度")
    @PostMapping("/addGeography")
    @ResponseBody
    public ResultEntity<Object> addGeography(@Validated @RequestBody GeographyDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addGeography(dto));
    }

}
