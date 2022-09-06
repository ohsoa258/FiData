package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppDTO;
import com.fisk.dataaccess.entity.DataTargetAppPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataTargetAppMap {

    DataTargetAppMap INSTANCES = Mappers.getMapper(DataTargetAppMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    DataTargetAppPO dtoToPo(DataTargetAppDTO dto);

    /**
     * po==>Dto
     *
     * @param po
     * @return
     */
    DataTargetAppDTO poToDto(DataTargetAppPO po);


}
