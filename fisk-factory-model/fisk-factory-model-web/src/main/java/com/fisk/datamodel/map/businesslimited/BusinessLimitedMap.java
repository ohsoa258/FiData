package com.fisk.datamodel.map.businesslimited;

import com.fisk.datamodel.dto.businesslimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businesslimited.BusinessLimitedDataAddDTO;
import com.fisk.datamodel.entity.businesslimited.BusinessLimitedPO;
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

    /**
     * dtoAdd==>Po
     * @param dto
     * @return
     */
    BusinessLimitedPO dtoAddToPo(BusinessLimitedDataAddDTO dto);

}
