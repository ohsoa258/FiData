package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleEditDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LifecycleMap {

    LifecycleMap INSTANCES = Mappers.getMapper(LifecycleMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "datasourceType.value", target = "datasourceType"),
            @Mapping(source = "tableState.value", target = "tableState"),
            @Mapping(source = "moduleState.value", target = "moduleState")
            //@Mapping(target = "componentNotificationDTOS", ignore = true)
    })
    LifecyclePO dtoToPo(LifecycleDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "datasourceType.value", target = "datasourceType"),
            @Mapping(source = "tableState.value", target = "tableState"),
            @Mapping(source = "moduleState.value", target = "moduleState")
            //@Mapping(target = "componentNotificationDTOS", ignore = true)
    })
    LifecyclePO dtoToPo_Edit(LifecycleEditDTO dto);
}