package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metadatabusinessmetadatamap.MetadataBusinessMetadataMapDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IMetadataBusinessMetadataMap {

    /**
     * 新增业务元数据
     *
     * @param dtoList
     * @return
     */
    ResultEnum addMetadataBusinessMetadataMap(List<MetadataBusinessMetadataMapDTO> dtoList);

    /**
     * 删除业务元数据
     *
     * @param metadataEntityId
     * @return
     */
    ResultEnum delMetadataBusinessMetadataMap(Integer metadataEntityId);

}
