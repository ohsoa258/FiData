package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataSourceConMap {
    DataSourceConMap INSTANCES = Mappers.getMapper(DataSourceConMap.class);

    /**
     * dto => po
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "conType.code", target = "conType")
    })
    DataSourceConPO dtoToPo(DataSourceConDTO dto);

}
