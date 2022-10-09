package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiParmDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiParmPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterApiParmVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗参数
 * @date 2022/10/8 16:56
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilterApiParmMap {
    BusinessFilterApiParmMap INSTANCES = Mappers.getMapper(BusinessFilterApiParmMap.class);
    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilterApiParmVO> poToVo(List<BusinessFilterApiParmPO> po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilterApiParmPO> dtoToPo(List<BusinessFilterApiParmDTO> dto);
}
