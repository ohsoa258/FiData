package com.fisk.dataaccess.map.apistate;

import com.fisk.dataaccess.dto.apistate.ApiStateDTO;
import com.fisk.dataaccess.entity.ApiStatePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author lsj
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiStateMap {

    ApiStateMap INSTANCES = Mappers.getMapper(ApiStateMap.class);


    /**
     * dto ==> po
     *
     * @param dto
     * @return
     */
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    ApiStatePO dtoToPo(ApiStateDTO dto);

    /**
     * po ==> dto
     *
     * @param po
     * @return
     */
    ApiStateDTO poToDto(ApiStatePO po);

}
