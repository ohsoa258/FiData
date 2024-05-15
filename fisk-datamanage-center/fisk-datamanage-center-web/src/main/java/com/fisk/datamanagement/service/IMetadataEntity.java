package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.metadata.dto.metadata.MetaDataBaseAttributeDTO;
import com.fisk.datamanagement.dto.metadataentity.DBTableFiledNameDto;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.dto.metadataentity.UpdateMetadataEmailGroupDTO;
import com.fisk.datamanagement.dto.metadataentity.UpdateMetadataExpiresTimeDto;
import com.fisk.datamanagement.dto.standards.StandardsSourceQueryDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author JianWenYang
 */
public interface IMetadataEntity {

    /**
     * 元数据新增
     *
     * @param dto
     * @param rdbmsType
     * @param parentEntityId
     * @return
     */
    Integer addMetadataEntity(MetaDataBaseAttributeDTO dto, String rdbmsType, String parentEntityId);

    /**
     * 元数据修改
     *
     * @param dto
     * @param entityId
     * @param rdbmsType
     * @return
     */
    Integer updateMetadataEntity(MetaDataBaseAttributeDTO dto, Integer entityId,String parentId,String rdbmsType);

    /**
     * 批量删除元数据实体
     *
     * @param ids
     * @return
     */
    ResultEnum delMetadataEntity(List<Integer> ids);

    /**
     * 通过表数据接入的表ID和字段Id查出元数据的字段
     * @param tableId
     * @param fldeId
     * @return
     */
    List<MetadataEntityDTO> queryFildes(Integer tableId, Integer fldeId);

    JSONObject getMetadataEntityDetailsV2(String entityId, String appName);


    /**
     *
     * @param fieldMetadataId
     * @return
     */
    DBTableFiledNameDto getParentNameByFieldId(Integer fieldMetadataId);

    DBTableFiledNameDto getParentNameByFieldIdV2(Integer fieldMetadataId);

    ResultEnum setMetadataExpiresTime(UpdateMetadataExpiresTimeDto dto);

    /**
     * 设置元数据邮箱组
     *
     * @param dto
     * @return
     */
    ResultEnum setMetadataEmailGroup(UpdateMetadataEmailGroupDTO dto);
}
