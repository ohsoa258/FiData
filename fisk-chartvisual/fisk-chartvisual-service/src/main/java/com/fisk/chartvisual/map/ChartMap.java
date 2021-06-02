package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.ChartPropertyDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.entity.ChartPO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.common.constants.SqlConstants;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author gy
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChartMap {

    ChartMap INSTANCES = Mappers.getMapper(ChartMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ChartPO dtoToPo(ReleaseChart dto);

    /**
     * 发布草稿，draftPo => chartPo
     *
     * @param draft draft
     * @param release release
     * @return release
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    ChartPO draftToRelease(DraftChartPO draft, @MappingTarget ChartPO release);
}
