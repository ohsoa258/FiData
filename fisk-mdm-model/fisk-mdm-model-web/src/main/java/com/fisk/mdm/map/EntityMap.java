package com.fisk.mdm.map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.entity.EntityPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author WangYan
 * @date 2022/3/9 17:20
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EntityMap {
    EntityMap INSTANCES = Mappers.getMapper(EntityMap.class);

    /**
     * po => dto
     * @param po
     * @return
     */
    EntityDTO poToDto(EntityPO po);

    /**
     * dto => ppo
     * @param dto
     * @return
     */
    EntityPO DtoToPo(EntityDTO dto);

    /**
     * poPage => dtoPage
     * @param page
     * @return
     */
    Page<EntityDTO> poToDtoPage(Page<EntityPO> page);

    /**
     * dto => po
     * @param dto
     * @return
     */
    EntityPO updateDtoToPo(UpdateEntityDTO dto);
}
