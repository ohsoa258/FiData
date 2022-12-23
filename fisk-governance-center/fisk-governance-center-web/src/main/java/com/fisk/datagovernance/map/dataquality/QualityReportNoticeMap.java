package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportNoticeDTO;
import com.fisk.datagovernance.entity.dataquality.QualityReportNoticePO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportNoticeVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QualityReportNoticeMap {

    QualityReportNoticeMap INSTANCES = Mappers.getMapper(QualityReportNoticeMap.class);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    QualityReportNoticeVO poToVo(QualityReportNoticePO po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    QualityReportNoticePO dtoToPo(QualityReportNoticeDTO dto);

}
