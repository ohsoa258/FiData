package com.fisk.mdm.map;

import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.vo.model.ModelVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ModelMap {
    ModelMap INSTANCES = Mappers.getMapper(ModelMap.class);


    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ModelPO dtoToPo(ModelDTO dto);


    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    ModelVO poToVo(ModelPO po);

}
