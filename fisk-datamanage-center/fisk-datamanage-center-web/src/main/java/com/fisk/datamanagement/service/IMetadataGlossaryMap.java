package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.dto.metadataglossarymap.GlossaryAndMetaDatasMapDTO;
import com.fisk.datamanagement.dto.metadataglossarymap.MetadataEntitySimpleDTO;
import com.fisk.datamanagement.entity.MetaDataGlossaryMapPO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IMetadataGlossaryMap  extends IService<MetaDataGlossaryMapPO> {

    /**
     * 业务术语-关联元数据id
     * @param dto
     * @return
     */
    Object mapGlossaryWithMetaEntity(GlossaryAndMetaDatasMapDTO dto);

    /**
     * 业务术语-回显该术语关联的所有元数据信息
     * @param glossaryId
     * @return
     */
    List<MetadataEntitySimpleDTO> getMetaEntitiesByGlossary(Integer glossaryId);
}
