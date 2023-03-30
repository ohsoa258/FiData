package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessFieldRuleDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessFieldRulePO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessFieldRuleVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessFieldRuleMap {
    BusinessFilter_ProcessFieldRuleMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessFieldRuleMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilter_ProcessFieldRulePO> dtoListToPoList(List<BusinessFilter_ProcessFieldRuleDTO> dto);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilter_ProcessFieldRuleVO> poListToVoList(List<BusinessFilter_ProcessFieldRulePO> po);
}
