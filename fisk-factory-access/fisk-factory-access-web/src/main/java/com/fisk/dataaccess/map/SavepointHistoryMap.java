package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.savepointhistory.SavepointHistoryDTO;
import com.fisk.dataaccess.entity.SavepointHistoryPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SavepointHistoryMap {

    SavepointHistoryMap INSTANCES = Mappers.getMapper(SavepointHistoryMap.class);

    /**
     * po==>Dto
     *
     * @param po
     * @return
     */
    SavepointHistoryDTO poToDto(SavepointHistoryPO po);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    SavepointHistoryPO dtoToPo(SavepointHistoryDTO dto);

    /**
     * poList==>DtoList
     *
     * @param poList
     * @return
     */
    List<SavepointHistoryDTO> poListToDtoList(List<SavepointHistoryPO> poList);

}
