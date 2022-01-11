package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.DataDoFieldDTO;
import com.fisk.chartvisual.dto.FieldDataDTO;
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
     * vo => dto
     * @param columnDetails
     * @return
     */
    List<DataDoFieldDTO> voToDto(List<FieldDataDTO> columnDetails);
}
