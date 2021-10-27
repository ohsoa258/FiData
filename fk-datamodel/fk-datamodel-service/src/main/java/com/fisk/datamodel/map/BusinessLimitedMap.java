package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedUpdateDTO;
import com.fisk.datamodel.entity.BusinessLimitedPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
/**
 * @author cfk
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessLimitedMap {
    BusinessLimitedMap INSTANCES = Mappers.getMapper(BusinessLimitedMap.class);
    /**
     * poToDto
     * @param businessLimitedPo
     * @return
     */
    BusinessLimitedDTO poToDto(BusinessLimitedPO businessLimitedPo);
    /**
     * dtoTopo
     * @param businessLimitedDto
     * @return
     */
    BusinessLimitedPO dtoTopo(BusinessLimitedDTO businessLimitedDto);
}
