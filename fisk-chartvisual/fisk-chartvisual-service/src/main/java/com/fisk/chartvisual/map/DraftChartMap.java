package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.ChartPropertyDTO;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.common.constants.SqlConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author gy
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DraftChartMap {
    DraftChartMap INSTANCES = Mappers.getMapper(DraftChartMap.class);


    /**
     * dto => po
     * @param dto source
     * @return target
     */
    DraftChartPO dtoToPo(ChartPropertyDTO dto);
}
