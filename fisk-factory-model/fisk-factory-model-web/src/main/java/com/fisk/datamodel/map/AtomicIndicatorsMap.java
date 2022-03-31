package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.atomicindicator.*;
import com.fisk.datamodel.entity.AtomicIndicatorsPO;
import com.fisk.datamodel.entity.IndicatorsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AtomicIndicatorsMap {
    AtomicIndicatorsMap INSTANCES = Mappers.getMapper(AtomicIndicatorsMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    List<IndicatorsPO> dtoToPo(List<AtomicIndicatorsDTO> dto);

    /**
     * po==>dtoList
     * @param poList
     * @return
     */
    List<AtomicIndicatorDropListDTO> poToDtoList(List<IndicatorsPO> poList);

    /**
     * poList==>dtoList
     * @param poList
     * @return
     */
    List<IndicatorsDataDTO> poListToDtoList(List<IndicatorsPO> poList);

    /**
     * po==>dto
     * @param po
     * @return
     */
    AtomicIndicatorsDetailDTO poToDto(IndicatorsPO po);


}
