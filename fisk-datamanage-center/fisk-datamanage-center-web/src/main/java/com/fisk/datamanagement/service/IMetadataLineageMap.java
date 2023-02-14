package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.metadatalineagemap.MetadataLineageMapDTO;

/**
 * @author JianWenYang
 */
public interface IMetadataLineageMap {

    /**
     * 新增process
     *
     * @param dto
     * @return
     */
    Long addMetadataLineageMap(MetadataLineageMapDTO dto);

}
