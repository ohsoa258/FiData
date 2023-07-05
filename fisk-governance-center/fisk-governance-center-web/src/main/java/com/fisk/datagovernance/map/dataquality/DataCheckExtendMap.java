package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckExtendDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckExtendPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验扩展属性
 * @date 2022/4/2 11:18
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataCheckExtendMap {

    DataCheckExtendMap INSTANCES = Mappers.getMapper(DataCheckExtendMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "rangeCheckType.value", target = "rangeCheckType"),
            @Mapping(source = "standardCheckType.value", target = "standardCheckType"),
            @Mapping(source = "fluctuateCheckType.value", target = "fluctuateCheckType"),
            @Mapping(source = "parentageCheckType.value", target = "parentageCheckType")
    })
    List<DataCheckExtendPO> dtoToPo(List<DataCheckExtendDTO> dto);
}
