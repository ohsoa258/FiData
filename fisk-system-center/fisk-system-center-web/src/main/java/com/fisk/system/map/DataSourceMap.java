package com.fisk.system.map;

import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.system.entity.DataSourcePO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataSourceMap {

    DataSourceMap INSTANCES = Mappers.getMapper(DataSourceMap.class);

    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true), // 某个属性不想映射，可以加上 ignore=true；
            @Mapping(source = "conType.value", target = "conType"),
            @Mapping(source = "sourceBusinessType.value", target = "sourceBusinessType"),
            @Mapping(target = "fileBinary",ignore = true)
    })
    void dtoToPo(DataSourceSaveDTO dto, @MappingTarget DataSourcePO po);

    /**
     * editDto => po
     *
     * @param dto source
     * @param po  target
     */
    @Mappings({
            @Mapping(source = "conType.value", target = "conType"),
            @Mapping(source = "sourceBusinessType.value", target = "sourceBusinessType"),
    })
    void accessDtoToPo(DataSourceSaveDTO dto, @MappingTarget DataSourcePO po);
}
