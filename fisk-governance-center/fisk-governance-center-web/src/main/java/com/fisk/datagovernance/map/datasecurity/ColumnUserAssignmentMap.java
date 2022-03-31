package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.columnuserassignment.ColumnUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnUserAssignmentPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ColumnUserAssignmentMap {

    ColumnUserAssignmentMap INSTANCES = Mappers.getMapper(ColumnUserAssignmentMap.class);

    /**
     * listDto==>ListPo
     * @param dtoList
     * @return
     */
    List<ColumnUserAssignmentPO> listDtoToListPo(List<ColumnUserAssignmentDTO> dtoList);

    /**
     * poList==>dtoList
     * @return
     */
    List<ColumnUserAssignmentDTO> poListToDto(List<ColumnUserAssignmentPO> poList);

}
