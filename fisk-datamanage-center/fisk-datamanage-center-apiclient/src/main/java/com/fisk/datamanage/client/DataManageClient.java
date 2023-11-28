package com.fisk.datamanage.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.BusinessMetaDataInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;
import com.fisk.datamanagement.dto.datamasking.SourceTableDataDTO;
import com.fisk.datamanagement.dto.dataquality.DataQualityDTO;
import com.fisk.datamanagement.dto.dataquality.UpperLowerBloodParameterDTO;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author JianWenYang
 */
@FeignClient("datamanagement-service")
public interface DataManageClient {

    /**
     * 数据源是否存在atlas
     * @param dto
     * @return
     */
    @PostMapping("/DataQuality/existAtlas")
    ResultEntity<Object> existAtlas(@Validated @RequestBody DataQualityDTO dto);

    /**
     * 是否存在上下血缘
     * @param dto
     * @return
     */
    @PostMapping("/DataQuality/existUpperLowerBlood")
    ResultEntity<Object> existUpperLowerBlood(@Validated @RequestBody UpperLowerBloodParameterDTO dto);

    /**
     * 数据脱敏，根据guid提供数据源信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/DataMasking/getSourceDataConfig")
    ResultEntity<DataMaskingTargetDTO> getSourceDataConfig(@Validated @RequestBody DataMaskingSourceDTO dto);

    /**
     * 根据guid获取数据工厂表信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/DataMasking/getTableData")
    ResultEntity<Object> getTableData(@Validated @RequestBody SourceTableDataDTO dto);

    /**
     * 元数据实时同步
     *
     * @param dto
     * @return
     */
    @PostMapping("/MetaData/metaData")
    ResultEntity<Object> metaData(@RequestBody MetaDataAttributeDTO dto);

    /**
     * 添加元数据实体
     *
     * @param dto
     * @return
     */
    @PostMapping("/MetaData/consumeMetaData")
    ResultEntity<Object> consumeMetaData(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto);

    /**
     * 同步表级业务元数据
     *
     * @param dto
     */
    @PostMapping("/BusinessMetaData/synchronousTableBusinessMetaData")
    void synchronousTableBusinessMetaData(@Validated @RequestBody BusinessMetaDataInfoDTO dto);

    /**
     * 删除元数据实体
     *
     * @param dto
     * @return
     */
    @DeleteMapping("/MetaData/deleteMetaData")
    ResultEntity<Object> deleteMetaData(@Validated @RequestBody MetaDataDeleteAttributeDTO dto);

    /**
     * 单个元数据信息消费
     * @param dto
     * @return
     */
    @PostMapping("/MetaData/addFiledAndUpdateFiled")
    ResultEntity<Object> addFiledAndUpdateFiled(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto);

    /**
     * 数据接入应用同步到业务分类
     *
     * @param dto
     * @return
     */
    @PostMapping("/Classification/appSynchronousClassification")
    ResultEntity<Object> appSynchronousClassification(@Validated @RequestBody ClassificationInfoDTO dto);

    /**
     * 日志记录
     * @param dto
     * @return
     */
    @PostMapping("/OperateLog/addOperateLog")
    ResultEntity<Object> saveLog(@RequestBody MetaDataEntityOperationLogDTO dto);

    /**
     * 根据IDS删除字段
     * @param ids
     * @return
     */
    @RequestMapping("/MetaData/fieldDelete")
    ResultEntity<Object> fieldDelete(@RequestParam("ids") List<Integer> ids);

    /**
     * 根据数据接入表ID和字段ID
     * @param tableId
     * @param fldeId
     * @return
     */
    @GetMapping("/MetaData/queryMetadaFildes/{tableId}/{fldeId}")
    List<MetadataEntityDTO> queryMetadaFildes(@PathVariable("tableId")Integer tableId, @PathVariable("fldeId")Integer fldeId);


    /**
     * 元数据实时同步
     *
     * @param entityList
     * @return
     */
    @PostMapping("/MetaData/syncDataConsumptionMetaData")
    ResultEntity<Object> syncDataConsumptionMetaData(@RequestBody  List<MetaDataEntityDTO> entityList);

    /**
     * 元数据实时同步
     *
     * @param entityList
     * @return
     */
    @PostMapping("/MetaData/deleteConsumptionMetaData")
    ResultEntity<Object> deleteConsumptionMetaData(@RequestBody  List<MetaDataEntityDTO> entityList);

}
