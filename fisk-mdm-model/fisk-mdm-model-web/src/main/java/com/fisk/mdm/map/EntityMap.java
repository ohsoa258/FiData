package com.fisk.mdm.map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.mdm.dto.attribute.AttributeStatusDTO;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.entity.EntityVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/9 17:20
 */

@Mapper(uses = { TypeConversionUtils.class },nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EntityMap {
    EntityMap INSTANCES = Mappers.getMapper(EntityMap.class);

    /**
     * po => dto
     * @param po
     * @return
     */
    @Mappings({
            @Mapping(source = "enableMemberLog" ,target = "enableMemberLog"),
            @Mapping(source = "status" ,target = "status")
    })
    EntityVO poToVo(EntityPO po);

    /**
     * dto => ppo
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "enableMemberLog" ,target = "enableMemberLog")
    })
    EntityPO DtoToPo(EntityDTO dto);

    /**
     * poPage => voPage
     * @param page
     * @return
     */
    Page<EntityVO> poToVoPage(Page<EntityPO> page);

    /**
     * dto => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "enableMemberLog" ,target = "enableMemberLog"),
            @Mapping(source = "status" ,target = "status")
    })
    EntityPO updateDtoToPo(UpdateEntityDTO dto);

    /**
     * voPage => poPage
     * @param page
     * @return
     */
    Page<EntityPO> voToPoPage(Page<EntityVO> page);

    List<EntityVO> poToVoList(List<EntityPO> entityPOS);

    /**
     * dto => po status
     * @param dto
     * @return
     */
    AttributePO dtoToStatusPo(AttributeStatusDTO dto);
}
