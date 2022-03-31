package com.fisk.datafactory.map;

import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.entity.NifiComponentsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NifiComponentMap {

    NifiComponentMap INSTANCES = Mappers.getMapper(NifiComponentMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    NifiComponentsPO dtoToPo(NifiComponentsDTO dto);

    /**
     * po => dto
     *
     * @param po source
     * @return target
     */
    NifiComponentsDTO poToDto(NifiComponentsPO po);

    /**
     * list集合 dto -> po
     *
     * @param list list
     * @return target
     */
    List<NifiComponentsPO> listDtoToPo(List<NifiComponentsDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list list
     * @return target
     */
    List<NifiComponentsDTO> listPoToDto(List<NifiComponentsPO> list);
}
