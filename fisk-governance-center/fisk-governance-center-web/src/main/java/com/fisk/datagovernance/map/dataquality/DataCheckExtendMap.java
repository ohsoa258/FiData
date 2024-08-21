package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckExtendDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckExtendPO;
import com.fisk.datagovernance.util.TypeConversionUtils;
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
@Mapper(uses ={TypeConversionUtils.class},nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
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
            @Mapping(source = "rangeCheckValueRangeType.value", target = "rangeCheckValueRangeType"),
            @Mapping(source = "rangeCheckKeywordIncludeType.value", target = "rangeCheckKeywordIncludeType"),
            @Mapping(source = "standardCheckCharRangeType.value", target = "standardCheckCharRangeType"),
            @Mapping(source = "fluctuateCheckType.value", target = "fluctuateCheckType"),
            @Mapping(source = "parentageCheckType.value", target = "parentageCheckType"),
    })
    DataCheckExtendPO dtoToPo(DataCheckExtendDTO dto);

    @Mappings({
            @Mapping(source = "rangeCheckType", target = "rangeCheckType"),
            @Mapping(source = "standardCheckType", target = "standardCheckType"),
            @Mapping(source = "rangeCheckValueRangeType", target = "rangeCheckValueRangeType"),
            @Mapping(source = "rangeCheckKeywordIncludeType", target = "rangeCheckKeywordIncludeType"),
            @Mapping(source = "standardCheckCharRangeType", target = "standardCheckCharRangeType"),
            @Mapping(source = "fluctuateCheckType", target = "fluctuateCheckType"),
            @Mapping(source = "parentageCheckType", target = "parentageCheckType"),
    })
    DataCheckExtendDTO poToDto(DataCheckExtendPO po);
    @Mappings({
            @Mapping(source = "rangeCheckType", target = "rangeCheckType"),
            @Mapping(source = "standardCheckType", target = "standardCheckType"),
            @Mapping(source = "rangeCheckValueRangeType", target = "rangeCheckValueRangeType"),
            @Mapping(source = "rangeCheckKeywordIncludeType", target = "rangeCheckKeywordIncludeType"),
            @Mapping(source = "standardCheckCharRangeType", target = "standardCheckCharRangeType"),
            @Mapping(source = "fluctuateCheckType", target = "fluctuateCheckType"),
            @Mapping(source = "parentageCheckType", target = "parentageCheckType"),
    })
    List<DataCheckExtendDTO> poListToDtoList(List<DataCheckExtendPO> po);
}
