package com.fisk.mdm.map;

import com.fisk.mdm.dto.attributeGroup.AttributeGroupDetailsDTO;
import com.fisk.mdm.dto.attributeGroup.UpdateAttributeGroupDTO;
import com.fisk.mdm.entity.AttributeGroupDetailsPO;
import com.fisk.mdm.entity.AttributeGroupPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author chenYa
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttributeGroupMap {

    AttributeGroupMap INSTANCES = Mappers.getMapper(AttributeGroupMap.class);

    /**
     * po => vo
     * @param po
     * @return
     */
    AttributeGroupVO groupPoToVo(AttributeGroupPO po);

    /**
     * dto => po
     * @param dto
     * @return
     */
    AttributeGroupPO groupDtoToPo(UpdateAttributeGroupDTO dto);

    /**
     * po => dto
     * @param poList
     * @return
     */
    List<AttributeGroupDetailsDTO> detailsPoToVoList(List<AttributeGroupDetailsPO> poList);
}



