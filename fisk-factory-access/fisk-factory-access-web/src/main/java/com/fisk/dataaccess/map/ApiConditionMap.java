package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.apicondition.ApiConditionDTO;
import com.fisk.dataaccess.dto.apicondition.ApiConditionDetailDTO;
import com.fisk.dataaccess.entity.ApiConditionPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiConditionMap {

    ApiConditionMap INSTANCES = Mappers.getMapper(ApiConditionMap.class);

    /**
     * poList==>DtoList
     *
     * @param poList
     * @return
     */
    List<ApiConditionDTO> poListToDtoList(List<ApiConditionPO> poList);

    /**
     * dto==>Detail
     *
     * @param dto
     * @return
     */
    ApiConditionDetailDTO dtoToDetail(ApiConditionDTO dto);

    /**
     * dtoList==>DetailList
     *
     * @param dto
     * @return
     */
    List<ApiConditionDetailDTO> dtoListToDetailList(List<ApiConditionDTO> dto);

}
