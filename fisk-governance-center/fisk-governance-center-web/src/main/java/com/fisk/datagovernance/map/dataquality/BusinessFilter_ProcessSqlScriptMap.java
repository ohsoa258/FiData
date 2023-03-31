package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessSqlScriptDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessSqlScriptPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessSqlScriptVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessFilter_ProcessSqlScriptMap {
    BusinessFilter_ProcessSqlScriptMap INSTANCES = Mappers.getMapper(BusinessFilter_ProcessSqlScriptMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<BusinessFilter_ProcessSqlScriptPO> dtoListToPoList(List<BusinessFilter_ProcessSqlScriptDTO> dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    BusinessFilter_ProcessSqlScriptPO dtoToPo(BusinessFilter_ProcessSqlScriptDTO dto);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<BusinessFilter_ProcessSqlScriptVO> poListToVoList(List<BusinessFilter_ProcessSqlScriptPO> po);
}
