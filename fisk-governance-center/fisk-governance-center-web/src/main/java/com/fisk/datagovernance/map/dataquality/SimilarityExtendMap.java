package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.datacheck.SimilarityExtendDTO;
import com.fisk.datagovernance.entity.dataquality.SimilarityExtendPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.SimilarityExtendVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验模块下相似度组件扩展属性Map
 * @date 2022/4/2 11:18
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SimilarityExtendMap {

    SimilarityExtendMap INSTANCES = Mappers.getMapper(SimilarityExtendMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    List<SimilarityExtendPO> dtoToPo(List<SimilarityExtendDTO> dto);

    /**
     * po => vo
     *
     * @param dto source
     * @return target
     */
    List<SimilarityExtendVO> poToVo(List<SimilarityExtendPO> dto);
}
