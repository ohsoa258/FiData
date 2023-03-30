package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessExpressDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessExpressPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessExpressVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessExpressMap {
    BusinessFilter_ProcessExpressMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessExpressMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilter_ProcessExpressPO> dtoListToPoList(List<BusinessFilter_ProcessExpressDTO> dto);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilter_ProcessExpressVO> poListToVoList(List<BusinessFilter_ProcessExpressPO> po);
}
