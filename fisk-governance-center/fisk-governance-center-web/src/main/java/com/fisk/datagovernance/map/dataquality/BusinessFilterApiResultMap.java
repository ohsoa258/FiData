package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiResultDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiResultPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterApiResultVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗结果
 * @date 2022/10/8 16:56
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilterApiResultMap {
    BusinessFilterApiResultMap INSTANCES = Mappers.getMapper(BusinessFilterApiResultMap.class);
    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilterApiResultVO> poToVo(List<BusinessFilterApiResultPO> po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilterApiResultPO> dtoToPo(List<BusinessFilterApiResultDTO> dto);
}
