package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckEditDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataCheckMap {

    DataCheckMap INSTANCES = Mappers.getMapper(DataCheckMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "ruleState.value", target = "ruleState"),
            @Mapping(source = "checkRule.value", target = "checkRule")
    })
    DataCheckPO dtoToPo(DataCheckDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "ruleState.value", target = "ruleState"),
            @Mapping(source = "checkRule.value", target = "checkRule")
    })
    DataCheckPO dtoToPo_Edit(DataCheckEditDTO dto);
}
