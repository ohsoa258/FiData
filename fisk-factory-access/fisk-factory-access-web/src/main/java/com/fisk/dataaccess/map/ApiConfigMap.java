package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.api.ApiConfigDTO;
import com.fisk.dataaccess.entity.ApiConfigPO;
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
public interface ApiConfigMap {
    ApiConfigMap INSTANCES = Mappers.getMapper(ApiConfigMap.class);

    /**
     * po -> dto
     *
     * @param po po
     * @return dto
     */
    ApiConfigDTO poToDto(ApiConfigPO po);

    /**
     * dto -> po
     *
     * @param dto dto
     * @return po
     */
    ApiConfigPO dtoToPo(ApiConfigDTO dto);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<ApiConfigDTO> listPoToDto(List<ApiConfigPO> list);

    /**
     * list: dto -> po
     *
     * @param list source
     * @return target
     */
    List<ApiConfigPO> listDtoToPo(List<ApiConfigDTO> list);
}
