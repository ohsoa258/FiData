package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.metadata.dto.metadata.MetaDataBaseAttributeDTO;
import io.swagger.models.auth.In;

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
    Integer updateMetadataEntity(MetaDataBaseAttributeDTO dto, Integer entityId, String rdbmsType);

    /**
     * 批量删除元数据实体
     *
     * @param ids
     * @return
     */
    ResultEnum delMetadataEntity(List<Integer> ids);



}
