package com.fisk.mdm.map;

import com.fisk.mdm.dto.mathingrules.SourceSystemMappingDto;
import com.fisk.mdm.entity.SourceSystemMappingPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JinXingWang
 */
@Mapper(uses = { TypeConversionUtils.class },nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SourceSystemMappingMap {
    SourceSystemMappingMap INSTANCES = Mappers.getMapper(SourceSystemMappingMap.class);

    List<SourceSystemMappingPO> dtoToPoList(List<SourceSystemMappingDto> dtoList);
}
