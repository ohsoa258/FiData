package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiParamDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiParamPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterApiParamVO;
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
public interface BusinessFilterApiParamMap {
    BusinessFilterApiParamMap INSTANCES = Mappers.getMapper(BusinessFilterApiParamMap.class);
    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilterApiParamVO> poToVo(List<BusinessFilterApiParamPO> po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilterApiParamPO> dtoToPo(List<BusinessFilterApiParamDTO> dto);
}
