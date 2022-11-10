package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.apiresultconfig.ApiResultConfigDTO;
import com.fisk.dataaccess.entity.ApiResultConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiResultConfigMap {

    ApiResultConfigMap INSTANCES = Mappers.getMapper(ApiResultConfigMap.class);

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<ApiResultConfigPO> dtoListToPoList(List<ApiResultConfigDTO> dtoList);

    /**
     * poList==>DtoList
     *
     * @param dtoList
     * @return
     */
    List<ApiResultConfigDTO> poListToDtoList(List<ApiResultConfigPO> dtoList);

}
