package com.fisk.datamanagement.service;

import com.fisk.common.service.metadata.dto.metadata.MetaDataBaseAttributeDTO;

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
    Integer updateMetadataEntity(MetaDataBaseAttributeDTO dto, Integer entityId, String rdbmsType);

}
