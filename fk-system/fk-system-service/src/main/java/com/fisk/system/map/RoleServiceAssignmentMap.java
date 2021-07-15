package com.fisk.system.map;

import com.fisk.system.dto.RoleServiceAssignmentDTO;
import com.fisk.system.entity.RoleServiceAssignmentPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleServiceAssignmentMap {
    RoleServiceAssignmentMap INSTANCES = Mappers.getMapper(RoleServiceAssignmentMap.class);

    /**
     * dto => po
     *
     * @param po
     * @return target
     */
    List<RoleServiceAssignmentDTO> poToDto(List<RoleServiceAssignmentPO> po);
}
