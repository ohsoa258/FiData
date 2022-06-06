package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckExtendDTO;
import com.fisk.datagovernance.entity.dataquality.DataCheckExtendPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckExtendVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验扩展属性
 * @date 2022/4/2 11:18
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataCheckExtendMap {

    DataCheckExtendMap INSTANCES = Mappers.getMapper(DataCheckExtendMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<DataCheckExtendPO> dtoToPo(List<DataCheckExtendDTO> dto);

    /**
     * po => vo
     *
     * @param dto source
     * @return target
     */
    List<DataCheckExtendVO> poToVo(List<DataCheckExtendPO> dto);
}
