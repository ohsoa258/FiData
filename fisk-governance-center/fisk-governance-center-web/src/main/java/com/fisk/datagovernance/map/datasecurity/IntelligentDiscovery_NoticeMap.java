package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_NoticeDTO;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_NoticePO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_NoticeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IntelligentDiscovery_NoticeMap {
    IntelligentDiscovery_NoticeMap INSTANCES = Mappers.getMapper(IntelligentDiscovery_NoticeMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "scanReceptionType.value", target = "scanReceptionType")
    })
    IntelligentDiscovery_NoticePO dtoToPo(IntelligentDiscovery_NoticeDTO dto);

    /**
     * po => vo
     *
     * @param dto source
     * @return target
     */
    IntelligentDiscovery_NoticeVO poToVo(IntelligentDiscovery_NoticePO dto);
}
