package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.app.AppApiBuiltinParmDTO;
import com.fisk.dataservice.entity.BuiltinParmPO;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiBuiltinParmMap {

    ApiBuiltinParmMap INSTANCES = Mappers.getMapper(ApiBuiltinParmMap.class);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<BuiltinParmPO> listDtoToPo(List<AppApiBuiltinParmDTO> list);
}
