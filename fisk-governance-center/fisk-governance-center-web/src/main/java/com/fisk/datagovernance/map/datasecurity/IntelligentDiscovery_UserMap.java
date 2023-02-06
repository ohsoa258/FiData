package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.intelligentdiscovery.IntelligentDiscovery_UserDTO;
import com.fisk.datagovernance.entity.datasecurity.IntelligentDiscovery_UserPO;
import com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery.IntelligentDiscovery_UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IntelligentDiscovery_UserMap {
    IntelligentDiscovery_UserMap INSTANCES = Mappers.getMapper(IntelligentDiscovery_UserMap.class);
    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<IntelligentDiscovery_UserVO> poToVo(List<IntelligentDiscovery_UserPO> po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<IntelligentDiscovery_UserPO> dtoToPo(List<IntelligentDiscovery_UserDTO> dto);
}