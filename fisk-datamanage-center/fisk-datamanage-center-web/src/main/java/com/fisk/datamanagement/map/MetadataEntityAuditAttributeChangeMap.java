package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.metaauditlog.MetadataEntityAuditAttributeChangeVO;
import com.fisk.datamanagement.entity.MetadataEntityAuditAttributeChangePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataEntityAuditAttributeChangeMap {
    MetadataEntityAuditAttributeChangeMap INSTANCES = Mappers.getMapper(MetadataEntityAuditAttributeChangeMap.class);

    MetadataEntityAuditAttributeChangeVO poToVo(MetadataEntityAuditAttributeChangePO po);

    List<MetadataEntityAuditAttributeChangeVO> poToVoList(List<MetadataEntityAuditAttributeChangePO> poList);
}
