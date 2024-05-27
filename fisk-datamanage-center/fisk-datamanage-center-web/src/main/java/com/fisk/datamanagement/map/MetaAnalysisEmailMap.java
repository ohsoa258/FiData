package com.fisk.datamanagement.map;


import com.fisk.datamanagement.dto.metaanalysisemailconfig.MetaAnalysisEmailConfigDTO;
import com.fisk.datamanagement.entity.MetaAnalysisEmailConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetaAnalysisEmailMap {

    MetaAnalysisEmailMap INSTANCES = Mappers.getMapper(MetaAnalysisEmailMap.class);

    MetaAnalysisEmailConfigDTO poToDto(MetaAnalysisEmailConfigPO po);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    MetaAnalysisEmailConfigPO dtoToPo(MetaAnalysisEmailConfigDTO po);

    List<MetaAnalysisEmailConfigDTO> posToDtos(List<MetaAnalysisEmailConfigPO> pos);

    List<MetaAnalysisEmailConfigPO> dtosToPos(List<MetaAnalysisEmailConfigDTO> pos);


}
