package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datacheck.DatacheckStandardsGroupDTO;
import com.fisk.datagovernance.entity.dataquality.DatacheckStandardsGroupPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DatacheckStandardsGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @Author: wangjian
 * @Date: 2024-04-24
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DatacheckStandardsGroupMap {

    DatacheckStandardsGroupMap INSTANCES = Mappers.getMapper(DatacheckStandardsGroupMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    DatacheckStandardsGroupPO dtoToPo(DatacheckStandardsGroupDTO dto);
    /**
     * po => dto
     *
     * @param po source
     * @return target
     */
    DatacheckStandardsGroupDTO poToDto(DatacheckStandardsGroupPO po);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    DatacheckStandardsGroupVO poToVo(DatacheckStandardsGroupPO po);
}
