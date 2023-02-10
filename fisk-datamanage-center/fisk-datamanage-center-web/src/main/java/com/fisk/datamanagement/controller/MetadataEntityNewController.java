package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.service.IMetadataEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/MetadataEntityNew")
public class MetadataEntityNewController {

    @Resource
    IMetadataEntity service;

    @ApiOperation("添加元数据实体")
    @PostMapping("/addMetadataEntity")
    public ResultEntity<Object> addEntity(@Validated @RequestBody MetaDataInstanceAttributeDTO dto) {
        return ResultEntityBuild.build(service.addMetadataEntity(dto));
    }

}
