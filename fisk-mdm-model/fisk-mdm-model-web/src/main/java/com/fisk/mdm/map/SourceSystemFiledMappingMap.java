package com.fisk.mdm.map;

import com.fisk.mdm.dto.mathingrules.SourceSystemFiledMappingDto;
import com.fisk.mdm.entity.SourceSystemFiledMappingPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JinXingWang
 */
@Mapper(uses = { TypeConversionUtils.class },nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SourceSystemFiledMappingMap {
    SourceSystemFiledMappingMap INSTANCES = Mappers.getMapper(SourceSystemFiledMappingMap.class);

    List<SourceSystemFiledMappingPO> dtoToPoList(List<SourceSystemFiledMappingDto> dtoList);
}
