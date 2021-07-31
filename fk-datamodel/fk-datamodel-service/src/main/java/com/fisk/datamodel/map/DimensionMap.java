package com.fisk.datamodel.map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionSourceDTO;
import com.fisk.datamodel.entity.DataAreaPO;
import com.fisk.datamodel.entity.DimensionPO;
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
     * 数据域po==>维度域dto
     * @param po
     * @return
     */
    List<DimensionSourceDTO> poToDtoList(List<DataAreaPO> po);

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

}
