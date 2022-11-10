package com.fisk.mdm.map;

import com.fisk.mdm.dto.codeRule.CodeRuleDTO;
import com.fisk.mdm.dto.codeRule.CodeRuleGroupDTO;
import com.fisk.mdm.dto.codeRule.CodeRuleGroupUpdateDTO;
import com.fisk.mdm.entity.CodeRuleGroupPO;
import com.fisk.mdm.entity.CodeRulePO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.codeRule.CodeRuleVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @Author WangYan
 * @Date 2022/7/1 10:25
 * @Version 1.0
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CodeRuleMap {

    CodeRuleMap INSTANCES = Mappers.getMapper(CodeRuleMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    CodeRuleGroupPO groupDtoToPo(CodeRuleGroupDTO dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    CodeRuleGroupPO groupUpdateDtoToPo(CodeRuleGroupUpdateDTO dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    CodeRulePO dtoToPo(CodeRuleDTO dto);

    /**
     * po => vo
     * @param po
     * @return
     */
    CodeRuleVO groupPoToVo(CodeRuleGroupPO po);

    /**
     * po => dto
     * @param po
     * @return
     */
    CodeRuleDTO detailsPoToDto(CodeRulePO po);
}
