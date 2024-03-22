package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.metaauditlog.MetadataEntityAuditLogVO;
import com.fisk.datamanagement.entity.MetadataEntityAuditLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JinXingWang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataEntityAuditLogMap {

    MetadataEntityAuditLogMap INSTANCES = Mappers.getMapper(MetadataEntityAuditLogMap.class);

    @Mapping(source = "createTime", target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    MetadataEntityAuditLogVO poToVo(MetadataEntityAuditLogPO po);

    List<MetadataEntityAuditLogVO> poToVoList(List<MetadataEntityAuditLogPO> poList);
}
