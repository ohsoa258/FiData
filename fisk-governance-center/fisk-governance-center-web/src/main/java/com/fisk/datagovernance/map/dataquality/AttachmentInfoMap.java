package com.fisk.datagovernance.map.dataquality;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 * @version 1.0
 * @description 附件Map
 * @date 2022/8/15 10:00
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttachmentInfoMap {
    AttachmentInfoMap INSTANCES = Mappers.getMapper(AttachmentInfoMap.class);
}
