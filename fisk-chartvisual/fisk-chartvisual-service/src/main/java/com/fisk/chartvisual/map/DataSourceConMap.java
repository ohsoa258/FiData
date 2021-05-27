package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.common.constants.SqlConstants;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataSourceConMap {

    DataSourceConMap INSTANCES = Mappers.getMapper(DataSourceConMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "conType.value", target = "conType"),
            @Mapping(target = "delFlag", constant = SqlConstants.NOT_DEL),
            @Mapping(target = "createTime", expression = "java(new java.util.Date())")
    })
    DataSourceConPO dtoToPo(DataSourceConDTO dto);


    /**
     * editDto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "conType.value", target = "conType"),
            @Mapping(target = "delFlag", constant = SqlConstants.NOT_DEL),
            @Mapping(target = "updateTime", expression = "java(new java.util.Date())")
    })
    void editDtoToPo(DataSourceConEditDTO dto, @MappingTarget DataSourceConPO po);
}
