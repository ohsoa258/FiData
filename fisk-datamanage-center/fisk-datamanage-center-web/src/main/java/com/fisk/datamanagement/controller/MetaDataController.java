package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.service.metadata.dto.metadata.MetaDataAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.synchronization.pushmetadata.IMetaData;
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
@RequestMapping("/MetaData")
public class MetaDataController {

    @Resource
    IMetaData service;

    @ApiOperation("元数据实时同步")
    @PostMapping("/metaData")
    public ResultEntity<Object> metaData(@RequestBody MetaDataAttributeDTO dto) {
        return ResultEntityBuild.build(service.metaData(dto));
    }

    @ApiOperation("添加元数据实体")
    @PostMapping("/consumeMetaData")
    public ResultEntity<Object> consumeMetaData(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto) {
        return ResultEntityBuild.build(service.consumeMetaData(dto));
    }

}
