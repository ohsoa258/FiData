package com.fisk.datamodel.map.businesslimited;

import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDTO;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDataDTO;
import com.fisk.datamodel.entity.businesslimited.BusinessLimitedAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author cfk
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessLimitedAttributeMap {
    BusinessLimitedAttributeMap INSTANCES = Mappers.getMapper(BusinessLimitedAttributeMap.class);
    /**
     * dtoToPo
     * @param businessLimitedAttributeDto
     * @return
     */
    BusinessLimitedAttributePO dtoToPo(BusinessLimitedAttributeDTO businessLimitedAttributeDto);
    /**
     * poToDto
     * @param businessLimitedAttributePo
     * @return
     */
    BusinessLimitedAttributeDTO poToDto(BusinessLimitedAttributePO businessLimitedAttributePo);

    /**
     * poList==>Dto
     * @param po
     * @return
     */
    List<BusinessLimitedAttributeDataDTO> poListToDto(List<BusinessLimitedAttributePO> po);

    /**
     * dto==>Po
     * @param dto
     * @return
     */
    BusinessLimitedAttributePO dtoToPo(BusinessLimitedAttributeDataDTO dto);


}
