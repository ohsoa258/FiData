package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;

/**
 * @author JianWenYang
 */
public interface IMetadataEntity {

    /**
     * 元数据新增
     *
     * @param dto
     * @return
     */
    ResultEnum addMetadataEntity(MetaDataInstanceAttributeDTO dto);

}
