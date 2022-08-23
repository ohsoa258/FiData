package com.fisk.datamodel.map.widetable;

import com.fisk.datamodel.dto.widetablerelationconfig.WideTableRelationConfigDTO;
import com.fisk.datamodel.entity.widetable.WideTableRelationConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WideTableRelationConfigMap {

    WideTableRelationConfigMap INSTANCES = Mappers.getMapper(WideTableRelationConfigMap.class);

    /**
     * dtoList==>poList
     *
     * @param dtoList
     * @return
     */
    List<WideTableRelationConfigPO> dtoToPo(List<WideTableRelationConfigDTO> dtoList);

}
