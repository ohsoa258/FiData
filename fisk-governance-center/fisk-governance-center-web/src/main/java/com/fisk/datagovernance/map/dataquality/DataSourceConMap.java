package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataSourceConEditDTO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
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
            @Mapping(source = "conType.value", target = "conType")
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
            @Mapping(source = "conType.value", target = "conType")
    })
    void editDtoToPo(DataSourceConEditDTO dto, @MappingTarget DataSourceConPO po);
}
