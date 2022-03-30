package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.RowUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.RowUserAssignmentPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RowUserAssignmentMap {

    RowUserAssignmentMap INSTANCES = Mappers.getMapper(RowUserAssignmentMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    RowUserAssignmentPO dtoToPo(RowUserAssignmentDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    RowUserAssignmentDTO poToDto(RowUserAssignmentPO dto);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<RowUserAssignmentDTO> listPoToDto(List<RowUserAssignmentPO> list);

    /**
     * list: dto -> po
     *
     * @param list source
     * @return target
     */
    List<RowUserAssignmentPO> listDtoToPo(List<RowUserAssignmentDTO> list);
}
