package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.metadatamapatlas.UpdateMetadataMapAtlasDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;

/**
 * @author JianWenYang
 */
public interface MetadataMapAtlasMapper extends FKBaseMapper<MetadataMapAtlasPO> {

    /**
     * 根据条件批量删除元数据存储atlas数据
     * @param dto
     * @return
     */
    int delBatchMetadataMapAtlas(UpdateMetadataMapAtlasDTO dto);

}
