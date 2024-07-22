package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckConditionDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckConditionPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckConditionVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 数据校验规则-检查条件
 * @date 2022/3/22 14:51
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataCheckConditionMap {
    DataCheckConditionMap INSTANCES = Mappers.getMapper(DataCheckConditionMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<DataCheckConditionPO> dtoListToPoList(List<DataCheckConditionDTO> dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    DataCheckConditionPO dtoToPo(DataCheckConditionDTO dto);

    /**
     * po => vo
     *
     * @param po source
     * @return target
     */
    List<DataCheckConditionVO> poListToVoList(List<DataCheckConditionPO> po);
}
