package com.fisk.task.map;

import com.fisk.task.dto.olap.OlapDto;
import com.fisk.task.entity.OlapDimensionPO;
import com.fisk.task.entity.OlapKpiPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JinXingWang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OlapMap {
    OlapMap INSTANCES = Mappers.getMapper(OlapMap.class);
    /**
     * po => dto
     * @param pos po
     * @return dto
     */
    @Mappings({
            @Mapping(source = "kpiTableName", target = "tableName"),
            @Mapping(source = "createKpiTableSql", target = "createTableSql"),
            @Mapping(source = "selectKpiDataSql", target = "selectDataSql"),
    })
    List<OlapDto> kpiPoToOlapDto(List<OlapKpiPO> pos);
    /**
     * po => dto
     * @param pos po
     * @return dto
     */
    @Mappings({
            @Mapping(source = "dimensionTableName", target = "tableName"),
            @Mapping(source = "createDimensionTableSql", target = "createTableSql"),
            @Mapping(source = "selectDimensionDataSql", target = "selectDataSql"),
    })
    List<OlapDto> dimensionPoToOlapDto(List<OlapDimensionPO> pos);
}
