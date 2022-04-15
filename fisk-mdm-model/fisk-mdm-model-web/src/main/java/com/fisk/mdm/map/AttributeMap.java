package com.fisk.mdm.map;

import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.vo.attribute.AttributeVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttributeMap {
    AttributeMap INSTANCES = Mappers.getMapper(AttributeMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    AttributePO dtoToPo(AttributeDTO dto);

    /**
     * po => vo
     * @param po
     * @return
     */
    AttributeVO poToVo(AttributePO po);

    /**
     * updateDTO => po
     * @param dto
     * @return
     */
    AttributePO updateDtoToPo(AttributeUpdateDTO dto);

    /**
     * po => dto
     * @param dto
     * @return
     */
    AttributeDTO poToDto(AttributePO dto);

    /**
     * po => dto list
     * @param list
     * @return
     */
    List<AttributeDTO> poToDtoList(List<AttributePO> list);
}
