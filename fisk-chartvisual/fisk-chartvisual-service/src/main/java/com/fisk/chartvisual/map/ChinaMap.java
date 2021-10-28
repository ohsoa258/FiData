package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.ChinaMapDTO;
import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.entity.ProvincialPO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

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
    List<ChinaMapDTO> poToDto(List<ProvincialPO> list);
}
