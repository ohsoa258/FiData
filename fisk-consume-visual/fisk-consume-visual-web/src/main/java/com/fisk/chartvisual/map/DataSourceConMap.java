package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.dataSource.DataSourceConDTO;
import com.fisk.chartvisual.dto.dataSource.DataSourceConEditDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author gy
 */
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
            @Mapping(source = "conType", target = "conType")
    })
    DataSourceConPO dtoToPo(DataSourceConDTO dto);


    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "conType", target = "conType")
    })
    void editDtoToPo(DataSourceConEditDTO dto, @MappingTarget DataSourceConPO po);


}
