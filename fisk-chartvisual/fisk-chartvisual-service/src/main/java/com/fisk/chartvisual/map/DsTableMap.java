package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.SaveDsTableDTO;
import com.fisk.chartvisual.entity.DsTableFieldPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author WangYan
 * @date 2022/3/9 17:20
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DsTableMap {
    DsTableMap INSTANCES = Mappers.getMapper(DsTableMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "id",target = "tableInfoId")
    })
    DsTableFieldPO dtoToPo(SaveDsTableDTO dto, Long id);
}
