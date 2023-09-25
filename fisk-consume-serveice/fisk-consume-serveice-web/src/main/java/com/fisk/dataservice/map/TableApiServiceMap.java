package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tableapi.TableApiServiceDTO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.util.TypeConversionUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @Author: wangjian
 * @Date: 2023-09-12
 * @Description:
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableApiServiceMap {
    TableApiServiceMap INSTANCES = Mappers.getMapper(TableApiServiceMap.class);
    /**
     * dto==>Po
     *
     * @param dto
     * @return
     */
    TableApiServicePO dtoToPo(TableApiServiceDTO dto);

    /**
     * Po==>Dto
     *
     * @param po
     * @return
     */
    TableApiServiceDTO poToDto(TableApiServicePO po);

}
