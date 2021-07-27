package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.ChartPropertyDTO;
import com.fisk.chartvisual.dto.ChartPropertyEditDTO;
import com.fisk.chartvisual.entity.BaseChartProperty;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author gy
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DraftChartMap {
    DraftChartMap INSTANCES = Mappers.getMapper(DraftChartMap.class);


    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    DraftChartPO dtoToPo(ChartPropertyDTO dto);

    /**
     * po => vo
     * @param po source
     * @return target vo
     */
    ChartPropertyVO poToVo(DraftChartPO po);

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
