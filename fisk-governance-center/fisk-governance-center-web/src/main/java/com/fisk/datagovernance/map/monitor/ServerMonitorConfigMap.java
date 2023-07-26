package com.fisk.datagovernance.map.monitor;

import com.fisk.datagovernance.dto.monitor.ServerMonitorConfigDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorConfigPO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @Author: wangjian
 * @Date: 2023-07-26
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServerMonitorConfigMap {
    ServerMonitorConfigMap INSTANCES = Mappers.getMapper(ServerMonitorConfigMap.class);
    @Mapping(source = "id",target = "id")
    ServerMonitorConfigPO dtoToPo(ServerMonitorConfigDTO dto);
    @Mapping(source = "id",target = "id")
    ServerMonitorConfigVO poToVO(ServerMonitorConfigPO po);
}
