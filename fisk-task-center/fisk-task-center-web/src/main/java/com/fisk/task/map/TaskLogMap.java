package com.fisk.task.map;


import com.fisk.task.dto.task.TaskLogDTO;
import com.fisk.task.entity.TaskLogPO;
import com.fisk.task.vo.TaskLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author gy
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskLogMap {

    TaskLogMap INSTANCES = Mappers.getMapper(TaskLogMap.class);

    /**
     * po => vo
     * @param po po
     * @return vo
     */
    @Mappings({
            @Mapping(source = "status.value", target = "status")
    })
    List<TaskLogVO> poToVo(List<TaskLogPO> po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    TaskLogPO dtoToPo(TaskLogDTO dto);

    /**
     * po => dto
     *
     * @param po source
     * @return target
     */
    TaskLogDTO poToDto(TaskLogPO po);

    /**
     * list集合 dto -> po
     *
     * @param list list
     * @return target
     */
    List<TaskLogPO> listDtoToPo(List<TaskLogDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list list
     * @return target
     */
    List<TaskLogDTO> listPoToDto(List<TaskLogPO> list);


}
