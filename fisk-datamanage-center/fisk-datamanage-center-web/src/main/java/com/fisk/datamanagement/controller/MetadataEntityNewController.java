package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.service.IEntity;
import com.fisk.datamanagement.synchronization.pushmetadata.IMetaData;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@RestController
@RequestMapping("/MetadataEntityNew")
public class MetadataEntityNewController {

    @Resource
    IMetaData service;
    @Resource
    IEntity entity;

    @ApiOperation("添加元数据实体")
    @PostMapping("/addMetadataEntity")
    public ResultEntity<Object> addEntity(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto) {
        return ResultEntityBuild.build(service.consumeMetaData(dto));
    }

    @ApiOperation("添加元数据实体")
    @GetMapping("/getEntityTreeList")
    public ResultEntity<Object> getEntityTreeList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, entity.getEntity("3"));
    }


}
