package com.fisk.datafactory.map;

import com.fisk.datafactory.dto.customworkflowdetail.TaskSettingDTO;
import com.fisk.datafactory.dto.taskschedule.TaskScheduleDTO;
import com.fisk.datafactory.entity.TaskSchedulePO;
import com.fisk.datafactory.entity.TaskSettingPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author SongJianJian
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskSettingMap {

    TaskSettingMap INSTANCES = Mappers.getMapper(TaskSettingMap.class);

    /**
     * list集合 ：po => dto
     * @param list source
     * @return list target
     */
    List<TaskSettingDTO> poToDto(List<TaskSettingPO> list);
}
