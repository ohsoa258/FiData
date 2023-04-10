package com.fisk.mdm.map;

import com.fisk.mdm.dto.mathingrules.MatchingRulesFiledDto;
import com.fisk.mdm.entity.MatchingRulesFiledPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JinXingWang
 */
@Mapper(uses = { TypeConversionUtils.class },nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MatchingRulesFiledMap {
    MatchingRulesFiledMap INSTANCES = Mappers.getMapper(MatchingRulesFiledMap.class);

    List<MatchingRulesFiledPO> dtoToPo(List<MatchingRulesFiledDto> dto);
}
