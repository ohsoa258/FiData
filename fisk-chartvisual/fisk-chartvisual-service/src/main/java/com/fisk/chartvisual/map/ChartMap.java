package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.ChartPropertyDTO;
import com.fisk.chartvisual.dto.ChartPropertyEditDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.entity.BaseChartProperty;
import com.fisk.chartvisual.entity.ChartPO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.chartvisual.vo.ChartPropertyVO;
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

    /**
     * @param po source
     * @return target vo
     */
    ChartPropertyVO poToVo(ChartPO po);

    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    void editDtoToPo(ChartPropertyEditDTO dto, @MappingTarget BaseChartProperty po);
}
