package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datacheck.ApiFieldDTO;
import com.fisk.datagovernance.entity.dataquality.DatacheckServerFieldConfigPO;
import com.fisk.datagovernance.util.TypeConversionUtils;
import com.fisk.datagovernance.vo.dataquality.datacheck.ApiFieldServerVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-11-29
 * @Description:
 */
@Mapper(uses = { TypeConversionUtils.class } ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DatacheckServerFieldConfigMap {

    DatacheckServerFieldConfigMap INSTANCES = Mappers.getMapper(DatacheckServerFieldConfigMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    DatacheckServerFieldConfigPO dtoToPo(ApiFieldDTO dto);

    /**
     * dtoList => poList
     *
     * @param dtoList source
     * @return target
     */
    List<DatacheckServerFieldConfigPO> dtoListToPoList(List<ApiFieldDTO> dtoList);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    ApiFieldServerVO poToVo(DatacheckServerFieldConfigPO po);

    /**
     * poList => voList
     *
     * @param poList source
     * @return target
     */
    List<ApiFieldServerVO> poListToVoList(List<DatacheckServerFieldConfigPO> poList);

}
