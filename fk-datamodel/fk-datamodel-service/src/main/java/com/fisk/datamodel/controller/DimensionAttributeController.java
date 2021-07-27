package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.datamodel.dto.ProjectDimensionAttributeDTO;
import com.fisk.datamodel.service.IProjectDimensionAttribute;
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
@Api(description = "数仓建模--维度字段")
@RestController
@RequestMapping("/attribute")
@Slf4j
public class DimensionAttributeController {
    @Resource
    IProjectDimensionAttribute service;

    @ApiOperation("添加维度字段")
    @PostMapping("/addAttribute")
    public ResultEntity<Object> addAttribute(@Validated @RequestBody List<ProjectDimensionAttributeDTO> dto)
    {
        return ResultEntityBuild.build(service.addProjectDimensionAttribute(dto));
    }

}
