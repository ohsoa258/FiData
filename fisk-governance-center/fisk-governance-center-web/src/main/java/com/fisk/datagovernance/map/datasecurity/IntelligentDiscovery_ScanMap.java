package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_ScanDTO;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_ScanPO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_ScanVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IntelligentDiscovery_ScanMap {
    IntelligentDiscovery_ScanMap INSTANCES = Mappers.getMapper(IntelligentDiscovery_ScanMap.class);
    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<IntelligentDiscovery_ScanVO> poToVo(List<IntelligentDiscovery_ScanPO> po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<IntelligentDiscovery_ScanPO> dtoToPo(List<IntelligentDiscovery_ScanDTO> dto);
}