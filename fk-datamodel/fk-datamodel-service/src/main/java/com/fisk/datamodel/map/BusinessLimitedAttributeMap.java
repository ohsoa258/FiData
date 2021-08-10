package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDTO;
import com.fisk.datamodel.entity.BusinessLimitedAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
/**
 * @author cfk
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessLimitedAttributeMap {
    BusinessLimitedAttributeMap INSTANCES = Mappers.getMapper(BusinessLimitedAttributeMap.class);
    BusinessLimitedAttributePO dtoTopo(BusinessLimitedAttributeDTO businessLimitedAttributeDTO);
    BusinessLimitedAttributeDTO poTodto(BusinessLimitedAttributePO businessLimitedAttributePO);

}
