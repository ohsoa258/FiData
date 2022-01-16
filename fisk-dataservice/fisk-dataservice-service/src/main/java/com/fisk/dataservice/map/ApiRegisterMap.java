package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.api.ApiConfigDTO;
import com.fisk.dataservice.dto.api.ApiConfigEditDTO;
import com.fisk.dataservice.entity.ApiConfigPO;
import com.fisk.dataservice.vo.api.ApiConfigVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiRegisterMap {

    ApiRegisterMap INSTANCES = Mappers.getMapper(ApiRegisterMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ApiConfigPO dtoToPo(ApiConfigDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ApiConfigPO dtoToPo_Edit(ApiConfigEditDTO dto);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    ApiConfigVO poToVo(ApiConfigPO po);
}
