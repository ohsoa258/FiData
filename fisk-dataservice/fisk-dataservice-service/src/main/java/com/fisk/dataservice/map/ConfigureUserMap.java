package com.fisk.dataservice.map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.dto.ConfigureDTO;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/2 15:35
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConfigureUserMap {

    ConfigureUserMap INSTANCES = Mappers.getMapper(ConfigureUserMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    ConfigureUserPO dtoToPo(UserDTO dto);

    /**
     * Page<ConfigureUserPO> page => Page<UserDTO> page
     * @param page
     * @return
     */
    Page<UserDTO> poToDtoPage(Page<ConfigureUserPO> page);

    /**
     * po => dt0
     * @param po
     * @return
     */
    List<ConfigureDTO> poToDto(List<ApiConfigurePO> po);
}
