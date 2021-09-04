package com.fisk.taskschedule.map;

import com.fisk.taskschedule.dto.TaskScheduleDTO;
import com.fisk.taskschedule.entity.TaskSchedulePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskScheduleMap {

    TaskScheduleMap INSTANCES = Mappers.getMapper(TaskScheduleMap.class);

    /**
     * dto => po
     * @param dto source
     * @return target
     */
    TaskSchedulePO dtoToPo(TaskScheduleDTO dto);

    /**
     * po => dto
     * @param po source
     * @return target
     */
    TaskScheduleDTO poToDto(TaskSchedulePO po);
}
