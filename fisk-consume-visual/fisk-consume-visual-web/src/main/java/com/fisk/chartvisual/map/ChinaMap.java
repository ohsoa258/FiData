package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.ChinaMapDTO;
import com.fisk.chartvisual.entity.ProvincialPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author wangyan
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChinaMap {

    ChinaMap INSTANCES = Mappers.getMapper(ChinaMap.class);

    /**
     * po => dto
     * @param list
     * @return
     */
    @Mappings({
            @Mapping(source = "provincialName",target = "name")
    })
    ChinaMapDTO poToDto(ProvincialPO list);
}
