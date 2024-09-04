package com.fisk.datamanagement.synchronization.pushmetadata;

import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.BusinessMetaDataInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.dto.metadataentity.ExportMetaDataDto;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author JianWenYang
 */
public interface IMetaData {

    /**
     * 新增元数据
     *
     * @param data
     * @return
     */
    ResultEnum metaData(MetaDataAttributeDTO data);

    /**
     * 消费元数据
     *
     * @param data
     * @return
     */
    ResultEnum consumeMetaData(List<MetaDataInstanceAttributeDTO> data, String currUserName, ClassificationTypeEnum classificationTypeEnumlong, Long syncTimeId);


    ResultEnum addFiledAndUpdateFiled(List<MetaDataInstanceAttributeDTO> data, ClassificationTypeEnum classificationTypeEnum);

    /**
     * 删除元数据实体
     *
     * @param dto
     * @return
     */
    ResultEnum deleteMetaData(MetaDataDeleteAttributeDTO dto);

    /**
     * 删除字段元数据实体
     *
     * @param dto
     * @return
     */
    ResultEnum deleteFieldMetaData(MetaDataDeleteAttributeDTO dto);

    /**
     * 同步表级业务元数据
     *
     * @param dto
     */
    void synchronousTableBusinessMetaData(BusinessMetaDataInfoDTO dto);


    /**
     * 导出
     *
     * @param dto
     */
    void export(ExportMetaDataDto dto, HttpServletResponse response);


    /**
     * 刷新Redis缓存key
     */
    void refreshRedisExcelMetadata();

    /**
     * 同步数据消费元数据
     *
     * @param entityList
     * @param currUserName
     * @return
     */
    ResultEnum syncDataConsumptionMetaData(List<MetaDataEntityDTO> entityList, String currUserName);

    /**
     * 删除数据消费元数据
     *
     * @param entityList
     * @return
     */
    ResultEnum deleteDataConsumptionMetaData(List<MetaDataEntityDTO> entityList);

}
