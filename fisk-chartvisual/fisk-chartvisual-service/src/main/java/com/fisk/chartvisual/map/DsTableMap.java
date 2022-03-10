package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.DsTableFieldDTO;
import com.fisk.chartvisual.entity.DsTableFieldPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

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
    DsTableFieldPO dtoToPo(DsTableFieldDTO dto, Long id);

    /**
     * po => dto
     * @param list
     * @return
     */
    List<DsTableFieldDTO> poToDtoDsList(List<DsTableFieldPO> list);
}
