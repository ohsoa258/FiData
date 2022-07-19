package com.fisk.datamanage.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.metadata.dto.metadata.MetaDataAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;
import com.fisk.datamanagement.dto.datamasking.SourceTableDataDTO;
import com.fisk.datamanagement.dto.dataquality.DataQualityDTO;
import com.fisk.datamanagement.dto.dataquality.UpperLowerBloodParameterDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
     * 删除元数据实体
     *
     * @param dto
     * @return
     */
    @DeleteMapping("/MetaData/deleteMetaData")
    ResultEntity<Object> deleteMetaData(@Validated @RequestBody MetaDataDeleteAttributeDTO dto);


}
