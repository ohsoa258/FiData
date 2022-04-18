package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilterMap {

    BusinessFilterMap INSTANCES = Mappers.getMapper(BusinessFilterMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "datasourceType.value", target = "datasourceType"),
            @Mapping(source = "filterStep.value", target = "filterStep"),
            @Mapping(source = "moduleState.value", target = "moduleState")
            //@Mapping(target = "componentNotificationDTOS", ignore = true)
    })
    BusinessFilterPO dtoToPo(BusinessFilterDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "datasourceType.value", target = "datasourceType"),
            @Mapping(source = "filterStep.value", target = "filterStep"),
            @Mapping(source = "moduleState.value", target = "moduleState")
            //@Mapping(target = "componentNotificationDTOS", ignore = true)
    })
    BusinessFilterPO dtoToPo_Edit(BusinessFilterEditDTO dto);
}