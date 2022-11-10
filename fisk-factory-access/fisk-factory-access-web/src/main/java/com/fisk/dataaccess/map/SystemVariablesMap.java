package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.entity.SystemVariablesPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SystemVariablesMap {

    SystemVariablesMap INSTANCES = Mappers.getMapper(SystemVariablesMap.class);

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<SystemVariablesPO> dtoListToPoList(List<DeltaTimeDTO> dtoList);

    /**
     * poList==>DtoList
     *
     * @param poList
     * @return
     */
    List<DeltaTimeDTO> poListToDtoList(List<SystemVariablesPO> poList);

}
