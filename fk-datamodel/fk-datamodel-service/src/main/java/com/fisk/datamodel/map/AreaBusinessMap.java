package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.AreaBusinessDTO;
import com.fisk.datamodel.entity.AreaBusinessPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AreaBusinessMap {
    AreaBusinessMap INSTANCES = Mappers.getMapper(AreaBusinessMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    AreaBusinessPO dtoToPo(AreaBusinessDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    AreaBusinessDTO poToDto(AreaBusinessPO po);

//    /**
//     * 集合: po ==> dto
//     *
//     * @param po List<>
//     * @return List
//     */
//    List<AreaBusinessDTO> poToDtos(List<AreaBusinessPO> po);

}
