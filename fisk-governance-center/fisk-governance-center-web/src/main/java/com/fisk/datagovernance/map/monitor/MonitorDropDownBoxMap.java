package com.fisk.datagovernance.map.monitor;

import com.fisk.datagovernance.entity.monitor.MonitorDropDownBoxPO;
import com.fisk.datagovernance.vo.monitor.MonitorDropDownBoxVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-07-10
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MonitorDropDownBoxMap {
    MonitorDropDownBoxMap INSTANCES = Mappers.getMapper(MonitorDropDownBoxMap.class);
    /**
     * po => vo
     *
     * @param dto source
     * @return target
     */
    MonitorDropDownBoxVO poToVo(MonitorDropDownBoxPO dto);
    /**
     * poList => voList
     *
     * @param dtoList source
     * @return target
     */
    List<MonitorDropDownBoxVO> poListToVoList(List<MonitorDropDownBoxPO> dtoList);
}
