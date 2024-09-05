package com.fisk.datagovernance.map.dataquality;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QualityReportRuleErrorLogMap {

    QualityReportRuleErrorLogMap INSTANCES = Mappers.getMapper(QualityReportRuleErrorLogMap.class);

}
