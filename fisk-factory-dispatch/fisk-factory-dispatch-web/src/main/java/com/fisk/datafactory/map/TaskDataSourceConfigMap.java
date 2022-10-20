package com.fisk.datafactory.map;

import com.fisk.datafactory.dto.taskdatasourceconfig.TaskDataSourceConfigDTO;
import com.fisk.datafactory.entity.TaskDataSourceConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskDataSourceConfigMap {

    TaskDataSourceConfigMap INSTANCES = Mappers.getMapper(TaskDataSourceConfigMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    TaskDataSourceConfigPO dtoToPo(TaskDataSourceConfigDTO dto);

}
