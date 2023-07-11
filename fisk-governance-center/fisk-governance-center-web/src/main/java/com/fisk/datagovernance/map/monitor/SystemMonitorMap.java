package com.fisk.datagovernance.map.monitor;

import com.fisk.datagovernance.dto.monitor.SystemMonitorDTO;
import com.fisk.datagovernance.entity.monitor.SystemMonitorPO;
import com.fisk.datagovernance.vo.monitor.SystemMonitorVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @Author: wangjian
 * @Date: 2023-07-06
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SystemMonitorMap {

    SystemMonitorMap INSTANCES = Mappers.getMapper(SystemMonitorMap.class);
    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    SystemMonitorPO dtoToPo(SystemMonitorDTO dto);
    /**
     * po => vo
     *
     * @param dto source
     * @return target
     */
    SystemMonitorVO poToVo(SystemMonitorPO dto);

}
