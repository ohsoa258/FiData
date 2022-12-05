package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportRuleDTO;
import com.fisk.datagovernance.entity.dataquality.QualityReportRulePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;


/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QualityReportRuleMap {

    QualityReportRuleMap INSTANCES = Mappers.getMapper(QualityReportRuleMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    QualityReportRulePO dtoToPo(QualityReportRuleDTO dto);
}