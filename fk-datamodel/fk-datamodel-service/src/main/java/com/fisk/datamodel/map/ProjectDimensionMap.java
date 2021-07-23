package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.ProjectDimensionDTO;
import com.fisk.datamodel.dto.ProjectDimensionSourceDTO;
import com.fisk.datamodel.entity.DataAreaPO;
import com.fisk.datamodel.entity.ProjectDimensionPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectDimensionMap {
    ProjectDimensionMap INSTANCES = Mappers.getMapper(ProjectDimensionMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    ProjectDimensionPO dtoToPo(ProjectDimensionDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    ProjectDimensionDTO poToDto(ProjectDimensionPO po);

    /**
     * 数据域po==>维度域dto
     * @param po
     * @return
     */
    List<ProjectDimensionSourceDTO> poToDtoList(List<DataAreaPO> po);

    /**
     * 数据域po==>数据域dto
     * @param po
     * @return
     */
    List<ProjectDimensionDTO> listPoToListDto(List<ProjectDimensionPO> po);

}
