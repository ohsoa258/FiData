package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.app.*;
import com.fisk.dataservice.entity.AppConfigPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppRegisterMap {

    AppRegisterMap INSTANCES = Mappers.getMapper(AppRegisterMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    AppConfigPO dtoToPo(AppRegisterDTO dto);

    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "appAccount", ignore = true),
            @Mapping(target = "appPassword", ignore = true)// 添加了ignore，表示不会对该属性做映射
    })
    void editDtoToPo(AppRegisterEditDTO dto, @MappingTarget AppConfigPO po);
}
