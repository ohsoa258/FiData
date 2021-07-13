package com.fisk.system.map;

import com.fisk.system.dto.UserDTO;
import com.fisk.system.entity.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;


/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMap {
    UserMap INSTANCES = Mappers.getMapper(UserMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    UserPO dtoToPo(UserDTO dto);

    /**
     po => dto
     */
    UserDTO poToDto(UserPO po);

    List<UserDTO> poToDtos(List<UserPO> po);


}
