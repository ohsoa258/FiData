package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.widetableconfig.WideTableConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigTaskDTO;
import com.fisk.datamodel.entity.WideTableConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WideTableMap {

    WideTableMap INSTANCES = Mappers.getMapper(WideTableMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    WideTableConfigPO dtoToPo(WideTableConfigDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    WideTableConfigDTO poToDto(WideTableConfigPO po);

    /**
     * po==>TaskDto
     * @param po
     * @return
     */
    WideTableFieldConfigTaskDTO poToTaskDto(WideTableConfigPO po);

}
