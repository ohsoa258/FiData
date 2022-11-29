package com.fisk.datafactory.map;

import com.fisk.datafactory.dto.customworkflow.DispatchEmailDTO;
import com.fisk.datafactory.entity.DispatchEmailPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author cfk
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DispatchEmailMap {

    DispatchEmailMap INSTANCES = Mappers.getMapper(DispatchEmailMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    DispatchEmailPO dtoToPo(DispatchEmailDTO dto);

    /**
     * po==>Dto
     *
     * @param po
     * @return
     */
    DispatchEmailDTO poToDto(DispatchEmailPO po);

    /**
     * list集合 dto -> po
     *
     * @param list list
     * @return target
     */
    List<DispatchEmailPO> listDtoToPo(List<DispatchEmailDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list list
     * @return target
     */
    List<DispatchEmailDTO> listPoToDto(List<DispatchEmailPO> list);
}
