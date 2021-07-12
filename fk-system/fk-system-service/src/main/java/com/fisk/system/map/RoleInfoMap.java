package com.fisk.system.map;

import com.fisk.system.dto.RoleInfoDTO;
import com.fisk.system.entity.RoleInfoPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleInfoMap {
    RoleInfoMap INSTANCES = Mappers.getMapper(RoleInfoMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    RoleInfoPO dtoToPo(RoleInfoDTO dto);

    /**
     po => dto
     */
    RoleInfoDTO poToDto(RoleInfoPO po);

    List<RoleInfoDTO> poToDtos(List<RoleInfoPO> po);

}
