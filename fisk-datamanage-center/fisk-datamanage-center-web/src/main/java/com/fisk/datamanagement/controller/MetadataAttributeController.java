package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.datamanagement.dto.metadataattribute.MetadataAttributeDTO;
import com.fisk.datamanagement.service.IMetadataAttribute;
import io.swagger.annotations.ApiOperation;
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
@RestController
@RequestMapping("/MetadataAttribute")
public class MetadataAttributeController {

    @Resource
    IMetadataAttribute service;

    @ApiOperation("添加自定义属性")
    @PostMapping("/metadataCustomAttribute")
    public ResultEntity<Object> metadataCustomAttribute(@Validated @RequestBody List<MetadataAttributeDTO> dto) {
        return ResultEntityBuild.build(service.metadataCustomAttribute(dto));
    }


}
