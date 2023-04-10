package com.fisk.mdm.map;

import com.fisk.mdm.dto.mathingrules.MatchingRulesDto;
import com.fisk.mdm.dto.mathingrules.UpdateMatchingRulesDto;
import com.fisk.mdm.entity.MatchingRulesPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JinXingWang
 */
@Mapper(uses = { TypeConversionUtils.class },nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MatchingRulesMap {

    MatchingRulesMap INSTANCES = Mappers.getMapper(MatchingRulesMap.class);

    /**
     * 添加 dto==>Po
     * @param dto
     * @return
     */
    MatchingRulesPO dtoToPo(MatchingRulesDto dto);

    /**
     * 修改 dto==>Po
     * @param dto
     * @return
     */
    MatchingRulesPO updateDtoToPo(UpdateMatchingRulesDto dto);
}
