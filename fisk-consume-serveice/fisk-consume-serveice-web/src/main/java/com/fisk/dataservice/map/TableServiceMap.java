package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.dataservice.entity.TableServicePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableServiceMap {

    TableServiceMap INSTANCES = Mappers.getMapper(TableServiceMap.class);

    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    TableServicePO dtoToPo(TableServiceDTO dto);

}
