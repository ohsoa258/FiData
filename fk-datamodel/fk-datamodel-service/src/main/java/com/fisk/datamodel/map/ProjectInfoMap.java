package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.ProjectInfoDropDTO;
import com.fisk.datamodel.entity.ProjectInfoPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectInfoMap {
    ProjectInfoMap INSTANCES = Mappers.getMapper(ProjectInfoMap.class);

    /**
     * po==>dto
     * @param po
     * @return
     */
    List<ProjectInfoDropDTO> poToDto(List<ProjectInfoPO> po);

}
