package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.metadata.dto.metadata.MetaDataAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.service.IMetadataEntity;
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
@RequestMapping("/MetaData")
public class MetaDataController {

    @Resource
    IMetaData service;

    @Resource
    IMetadataEntity iMetadataEntity;
    @ApiOperation("根据接入表ID+字段ID查找对应元数据中的字段")
    @GetMapping("/queryMetadaFildes/{tableId}/{fldeId}")
    public List<MetadataEntityDTO> queryMetadaFildes(@PathVariable("tableId")Integer tableId, @PathVariable("fldeId")Integer fldeId){
        return iMetadataEntity.queryFildes(tableId,fldeId);
    }

    @ApiOperation("元数据实时同步")
    @PostMapping("/metaData")
    public ResultEntity<Object> metaData(@RequestBody MetaDataAttributeDTO dto) {
        return ResultEntityBuild.build(service.metaData(dto));
    }

    @ApiOperation("添加元数据实体")
    @PostMapping("/consumeMetaData")
    public ResultEntity<Object> consumeMetaData(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto) {
        return ResultEntityBuild.build(service.consumeMetaData(dto,dto.get(0).currUserName));
    }
    @ApiOperation("元数据字段新增或修改字段")
    @PostMapping("/addFiledAndUpdateFiled")
    public ResultEntity<Object> addFiledAndUpdateFiled(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto){
        return ResultEntityBuild.build(service.addFiledAndUpdateFiled(dto));
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

    @ApiOperation("test")
    @PostMapping("/test")
    public void test() {
        service.test();
    }

}
