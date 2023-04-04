package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessTaskDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessTaskPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessTaskMap {
    BusinessFilter_ProcessTaskMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessTaskMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilter_ProcessTaskPO> dtoListToPoList(List<BusinessFilter_ProcessTaskDTO> dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    BusinessFilter_ProcessTaskPO dtoToPo(BusinessFilter_ProcessTaskDTO dto);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilter_ProcessTaskVO> poListToVoList(List<BusinessFilter_ProcessTaskPO> po);
}
