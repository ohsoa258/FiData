package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnSecurityConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ColumnSecurityConfigMap {

    ColumnSecurityConfigMap INSTANCES = Mappers.getMapper(ColumnSecurityConfigMap.class);

    /**
     * po==>AssignmentDto
     * @param po
     * @return
     */
    ColumnSecurityConfigUserAssignmentDTO poToAssignmentDto(ColumnSecurityConfigPO po);

    /**
     * dto==>Po
     * @param dto
     * @return
     */
    ColumnSecurityConfigPO dtoToPo(ColumnSecurityConfigUserAssignmentDTO dto);

}
