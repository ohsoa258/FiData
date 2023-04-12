package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.qualityreport.QualityReportRecipientDTO;
import com.fisk.datagovernance.entity.dataquality.QualityReportRecipientPO;
import com.fisk.datagovernance.vo.dataquality.qualityreport.QualityReportRecipientVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QualityReportRecipientMap {

    QualityReportRecipientMap INSTANCES = Mappers.getMapper(QualityReportRecipientMap.class);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<QualityReportRecipientVO> poToVo(List<QualityReportRecipientPO> po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<QualityReportRecipientPO> dtoToPo(List<QualityReportRecipientDTO> dto);
}
