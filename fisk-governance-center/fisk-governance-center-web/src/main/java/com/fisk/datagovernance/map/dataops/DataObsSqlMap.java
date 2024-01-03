package com.fisk.datagovernance.map.dataops;

import com.fisk.datagovernance.dto.dataops.DataObsSqlDTO;
import com.fisk.datagovernance.entity.dataops.DataObsSqlPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-01-03
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataObsSqlMap {
    DataObsSqlMap INSTANCES = Mappers.getMapper(DataObsSqlMap.class);

    List<DataObsSqlDTO> poListToDtoList(List<DataObsSqlPO> list);

    List<DataObsSqlPO> dtoListToPoList(List<DataObsSqlDTO> list);
}
