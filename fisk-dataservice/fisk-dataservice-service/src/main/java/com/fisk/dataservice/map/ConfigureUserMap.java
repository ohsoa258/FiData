package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author WangYan
 * @date 2021/8/2 15:35
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConfigureUserMap {

    ConfigureUserMap INSTANCES = Mappers.getMapper(ConfigureUserMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ConfigureUserPO dtoToPo(UserDTO dto);
}
