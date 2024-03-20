package com.fisk.datamanagement.controller;

import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.metadataentity.ExportMetaDataDto;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.service.IMetadataEntity;
import com.fisk.datamanagement.synchronization.pushmetadata.IMetaData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author JianWenYang
 */

@Api(tags = {SwaggerConfig.MetaData})
@RestController
@RequestMapping("/MetaData")
public class MetaDataController {

    @Resource
    IMetaData service;

    @Resource
    IMetadataEntity iMetadataEntity;

    @ApiOperation("根据接入表ID+字段ID查找对应元数据中的字段")
    @GetMapping("/queryMetadaFildes/{tableId}/{fldeId}")
    public List<MetadataEntityDTO> queryMetadaFildes(@PathVariable("tableId") Integer tableId, @PathVariable("fldeId") Integer fldeId) {
        return iMetadataEntity.queryFildes(tableId, fldeId);
    }

    @ApiOperation("元数据实时同步")
    @PostMapping("/metaData")
    public ResultEntity<Object> metaData(@RequestBody MetaDataAttributeDTO dto) {
        return ResultEntityBuild.build(service.metaData(dto));
    }

    @ApiOperation("添加元数据实体")
    @PostMapping("/consumeMetaData")
    public ResultEntity<Object> consumeMetaData(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto, @RequestParam ClassificationTypeEnum classificationTypeEnum) {
        return ResultEntityBuild.build(service.consumeMetaData(dto, dto.get(0).currUserName, classificationTypeEnum));
    }

    @ApiOperation("元数据字段新增或修改字段")
    @PostMapping("/addFiledAndUpdateFiled")
    public ResultEntity<Object> addFiledAndUpdateFiled(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto, @RequestParam ClassificationTypeEnum classificationTypeEnum) {
        return ResultEntityBuild.build(service.addFiledAndUpdateFiled(dto, classificationTypeEnum));
    }

    @ApiOperation("导出元数据")
    @PostMapping(path = "/export")
    @ControllerAOPConfig(printParams = false)
    public void export(@Validated @RequestBody ExportMetaDataDto dto, HttpServletResponse response) {
        service.export(dto, response);
    }

    @ApiOperation("刷新导出元数据Redis缓存")
    @PostMapping(path = "/refreshRedisExcelMetadata")
    public void refreshRedisExcelMetadata() {
        service.refreshRedisExcelMetadata();
    }


    @ApiOperation("删除元数据实体")
    @DeleteMapping("/deleteMetaData")
    public ResultEntity<Object> deleteMetaData(@Validated @RequestBody MetaDataDeleteAttributeDTO dto) {
        return ResultEntityBuild.build(service.deleteMetaData(dto));
    }

    @ApiOperation("删除元数据字段实体")
    @RequestMapping("/fieldDelete")
    public ResultEntity<Object> fieldDelete(@RequestParam("ids") List<Integer> ids) {
        return ResultEntityBuild.build(iMetadataEntity.delMetadataEntity(ids));
    }


    @ApiOperation("添加数据消费元数据")
    @PostMapping("/syncDataConsumptionMetaData")
    public ResultEntity<Object> syncDataConsumptionMetaData(@RequestBody List<MetaDataEntityDTO> entityList) {
        return ResultEntityBuild.build(service.syncDataConsumptionMetaData(entityList, ""));
    }


    @ApiOperation("删除数据消费元数据")
    @PostMapping("/deleteConsumptionMetaData")
    public ResultEntity<Object> deleteConsumptionMetaData(@RequestBody List<MetaDataEntityDTO> entityList) {
        return ResultEntityBuild.build(service.deleteDataConsumptionMetaData(entityList));
    }

}
