package com.fisk.datagovernance.map.monitor;

import com.fisk.datagovernance.dto.monitor.ServerMonitorDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-06
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServerMonitorMap {

    ServerMonitorMap INSTANCES = Mappers.getMapper(ServerMonitorMap.class);
    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ServerMonitorPO dtoToPo(ServerMonitorDTO dto);
    /**
     * dtoList => poList
     *
     * @param dtoList source
     * @return target
     */
    List< ServerMonitorPO> dtoListToPoList(List< ServerMonitorDTO> dtoList);
}
