package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiConfigDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiConfigPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterApiConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗
 * @date 2022/10/8 16:55
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilterApiMap {
    BusinessFilterApiMap INSTANCES = Mappers.getMapper(BusinessFilterApiMap.class);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    BusinessFilterApiConfigVO poToVo(BusinessFilterApiConfigPO po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    BusinessFilterApiConfigPO dtoToPo(BusinessFilterApiConfigDTO dto);
}
