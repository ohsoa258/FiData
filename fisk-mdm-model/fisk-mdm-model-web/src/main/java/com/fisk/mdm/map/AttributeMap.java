package com.fisk.mdm.map;

import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.utlis.BooleanToIntUtils;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author chenYa
 */
@Mapper( uses = { BooleanToIntUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttributeMap {
    AttributeMap INSTANCES = Mappers.getMapper(AttributeMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "enableAttributeLog" ,target = "enableAttributeLog"),
            @Mapping(source = "enableReadonly" ,target = "enableReadonly"),
            @Mapping(source = "enableRequired" ,target = "enableRequired")
    })
    AttributePO dtoToPo(AttributeDTO dto);

    /**
     * po => vo
     * @param po
     * @return
     */
    @Mappings({
            @Mapping(source = "enableAttributeLog" ,target = "enableAttributeLog"),
            @Mapping(source = "enableReadonly" ,target = "enableReadonly"),
            @Mapping(source = "enableRequired" ,target = "enableRequired")
    })
    AttributeVO poToVo(AttributePO po);

    /**
     * updateDTO => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "enableReadonly" ,target = "enableAttributeLog"),
            @Mapping(source = "enableRequired" ,target = "enableRequired")
    })
    AttributePO updateDtoToPo(AttributeUpdateDTO dto);


    /**
     * poList => DtoList
     *
     * @param list 列表
     * @return {@link List}<{@link AttributeInfoDTO}>
     */
    List<AttributeInfoDTO> poToDtoList(List<AttributePO> list);

    /**
     * entityPo => EntityInfoVO
     *
     * @param entityPo 实体po
     * @return {@link EntityInfoVO}
     */
    EntityInfoVO poToEntityVo(EntityPO entityPo);

}



