package com.fisk.mdm.map;

import com.fisk.mdm.dto.access.AccessAttributeDTO;
import com.fisk.mdm.entity.AccessTransformationPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-04-19
 * @Description:
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccessTransformationMap {

    AccessTransformationMap INSTANCES = Mappers.getMapper(AccessTransformationMap.class);

    /**
     * po => dto
     * @param po
     * @return
     */
    List<AccessAttributeDTO> poListToDtoList(List<AccessTransformationPO> po);
    /**
     * dto => po
     * @param dto
     * @return
     */
    List<AccessTransformationPO> dtoListToPoList(List<AccessAttributeDTO> dto);
}
