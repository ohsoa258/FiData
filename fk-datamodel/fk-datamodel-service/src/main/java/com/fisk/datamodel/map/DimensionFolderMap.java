package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDataDTO;
import com.fisk.datamodel.entity.DimensionFolderPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DimensionFolderMap {

    DimensionFolderMap INSTANCES = Mappers.getMapper(DimensionFolderMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    DimensionFolderPO dtoToPo(DimensionFolderDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    DimensionFolderDTO poToDto(DimensionFolderPO po);

    /**
     * poList==>dtoList
     * @param po
     * @return
     */
    List<DimensionFolderDataDTO> poListToDtoList(List<DimensionFolderPO> po);

}
