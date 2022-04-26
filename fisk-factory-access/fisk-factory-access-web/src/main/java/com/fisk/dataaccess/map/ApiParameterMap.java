package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.api.ApiParameterDTO;
import com.fisk.dataaccess.entity.ApiParameterPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/17 14:51
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiParameterMap {
    ApiParameterMap INSTANCES = Mappers.getMapper(ApiParameterMap.class);

    /**
     * po -> dto
     *
     * @param po po
     * @return dto
     */
    ApiParameterDTO poToDto(ApiParameterPO po);

    /**
     * dto -> po
     *
     * @param dto dto
     * @return po
     */
    ApiParameterPO dtoToPo(ApiParameterDTO dto);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<ApiParameterDTO> listPoToDto(List<ApiParameterPO> list);

    /**
     * list: dto -> po
     *
     * @param list source
     * @return target
     */
    List<ApiParameterPO> listDtoToPo(List<ApiParameterDTO> list);
}
