package com.fisk.datagovernance.map.monitor;

import com.fisk.datagovernance.dto.monitor.ServerMonitorTypeDTO;
import com.fisk.datagovernance.entity.monitor.ServerMonitorTypePO;
import com.fisk.datagovernance.vo.monitor.ServerMonitorTypeVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-10-11
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServerMonitorTypeMap {

    ServerMonitorTypeMap INSTANCES = Mappers.getMapper(ServerMonitorTypeMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ServerMonitorTypePO dtoToPo(ServerMonitorTypeDTO dto);

    /**
     * dtoList => poList
     *
     * @param dtoList source
     * @return target
     */
    List< ServerMonitorTypePO> dtoListToPoList(List< ServerMonitorTypeDTO> dtoList);


    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    ServerMonitorTypeVO poToVo(ServerMonitorTypePO po);

    /**
     * poList => voList
     *
     * @param pos source
     * @return target
     */
    List<ServerMonitorTypeVO> poListToVoList(List<ServerMonitorTypePO> pos);

}
