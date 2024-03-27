package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.dto.metadataglossarymap.GlossaryAndMetaDatasMapDTO;
import com.fisk.datamanagement.entity.MetaDataGlossaryMapPO;

/**
 * @author JianWenYang
 */
public interface IMetadataGlossaryMap  extends IService<MetaDataGlossaryMapPO> {

    Object mapGlossaryWithMetaEntity(GlossaryAndMetaDatasMapDTO dto);
}
