package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportDTO;
import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportEditDTO;
import com.fisk.datagovernance.entity.dataquality.QualityReportPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QualityReportMap {

    QualityReportMap INSTANCES = Mappers.getMapper(QualityReportMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    QualityReportPO dtoToPo(QualityReportDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    QualityReportPO dtoToPo_Edit(QualityReportEditDTO dto);
}