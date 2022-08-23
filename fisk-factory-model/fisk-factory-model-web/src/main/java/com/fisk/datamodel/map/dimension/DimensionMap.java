package com.fisk.datamodel.map.dimension;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimension.DimensionListDTO;
import com.fisk.datamodel.dto.dimension.DimensionSelectDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionMetaDTO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DimensionMap {
    DimensionMap INSTANCES = Mappers.getMapper(DimensionMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    DimensionPO dtoToPo(DimensionDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    DimensionDTO poToDto(DimensionPO po);

    /**
     * 数据域po==>数据域dto
     * @param po
     * @return
     */
    List<DimensionDTO> listPoToListDto(List<DimensionPO> po);

    /**
     * 分页po==>dto
     * @param po
     * @return
     */
    Page<DimensionDTO> pagePoToDto(IPage<DimensionPO> po);

    /**
     * 维度表PO==>维度DTO
     * @param po
     * @return
     */
    List<DimensionListDTO> listPoToListsDto(List<DimensionPO> po);

    /**
     * po==>ListNameDto
     * @param po
     * @return
     */
    List<DimensionMetaDTO> poToListNameDto(List<DimensionPO> po);

    /**
     * po==>SelectDtoList
     *
     * @param po source
     * @return target
     */
    List<DimensionSelectDTO> listPoToListSelectDto(List<DimensionPO> po);
}
