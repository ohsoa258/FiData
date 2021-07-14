package com.fisk.system.map;

import com.fisk.system.dto.RoleUserAssignmentDTO;
import com.fisk.system.entity.RoleUserAssignmentPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleUserAssignmentMap {
    RoleUserAssignmentMap INSTANCES = Mappers.getMapper(RoleUserAssignmentMap.class);

    /**
     * dto => po
     *
     * @param po
     * @return target
     */
    List<RoleUserAssignmentDTO> poToDto(List<RoleUserAssignmentPO> po);

}
