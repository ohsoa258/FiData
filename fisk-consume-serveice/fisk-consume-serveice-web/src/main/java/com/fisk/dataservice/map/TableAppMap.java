package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tableservice.TableAppDTO;
import com.fisk.dataservice.entity.TableAppPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableAppMap {
    TableAppMap INSTANCES = Mappers.getMapper(TableAppMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "appType.value", target = "appType"),
            @Mapping(source = "interfaceType.value", target = "interfaceType"),
            @Mapping(source = "authenticationType.value", target = "authenticationType"),
            @Mapping(source = "authType.value", target = "authType"),
            @Mapping(source = "requestType.value", target = "requestType")
    })
    TableAppPO dtoToPo(TableAppDTO dto);
}
