package com.fisk.dataservice.map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.dto.ApiConfigureDTO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author WangYan
 * @date 2021/8/3 16:58
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiConfigureMap {

    ApiConfigureMap INSTANCES = Mappers.getMapper(ApiConfigureMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ApiConfigurePO dtoToPo(ApiConfigureDTO dto);

    /**
     * Page<ApiConfigurePO> page => Page<ApiConfigureDTO> page
     * @param page
     * @return
     */
    Page<ApiConfigureDTO> poToDtoPage(Page<ApiConfigurePO> page);
}
