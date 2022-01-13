package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.enums.FieldTypeEnum;
import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.common.enums.chartvisual.ColumnTypeEnum;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/1/11 10:37
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VisualizationMap {
    VisualizationMap INSTANCES = Mappers.getMapper(VisualizationMap.class);


    /**
     * FieldDataDTO => DataDoFieldDTO
     * @param columnDetails
     * @return
     */
    @Mappings({
            @Mapping(source = "columnName",target = "fieldName"),
            @Mapping(source = "fieldTableName",target = "tableName")
    })
    DataDoFieldDTO dataDoField(FieldDataDTO columnDetails);

    /**
     * vo => dto
     * @param columnDetails
     * @return
     */
    List<DataDoFieldDTO> dataDoFields(List<FieldDataDTO> columnDetails);

    /**
     * 枚举转换
     * @param type
     * @return
     */
    @ValueMappings({
            @ValueMapping(source = "COLUMN",target = "NAME"),
            @ValueMapping(source = "VALUE",target = "VALUE"),
            @ValueMapping(source=MappingConstants.ANY_UNMAPPED, target=MappingConstants.NULL)
    })
    ColumnTypeEnum toEnum(FieldTypeEnum type);


    /**
     * type转换
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "fieldType",target = "columnType")
    })
    ColumnDetails toColumn(FieldDataDTO dto);

    /**
     * ChartQueryObjectVO => ChartQueryObject
     * @param object
     * @return
     */
    ChartQueryObject dataDoObject(ChartQueryObjectVO object);


    /***
     * FieldDataDTO => ColumnDetailsSsas
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "columnName",target = "name"),
            @Mapping(source = "columnLabel",target = "uniqueName"),
            @Mapping(source = "fieldType",target = "dragElemType"),
            @Mapping(source = "dimension",target = "dimensionType")
    })
    ColumnDetailsSsas dtoToColumnDetails(FieldDataDTO dto);


    /**
     * ChartQueryObjectVO => ChartQueryObjectSsas
     * @param objectVO
     * @return
     */
    ChartQueryObjectSsas dataToObjectSsas(ChartQueryObjectVO objectVO);
}
