package com.fisk.datagovern.map;

import com.fisk.datagovern.dto.category.CategoryDTO;
import com.fisk.datagovern.dto.category.CategoryDataDTO;
import com.fisk.datagovern.entity.CategoryPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMap {
    CategoryMap INSTANCES = Mappers.getMapper(CategoryMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    CategoryPO dtoToPo(CategoryDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    CategoryDTO poToDto(CategoryPO po);

    /**
     * dataListPo==>Dto
     * @param po
     * @return
     */
    List<CategoryDataDTO> dataListPoToDto(List<CategoryPO> po);

    /**
     * dataPo==>Dto
     * @param po
     * @return
     */
    CategoryDataDTO dataPoToDto(CategoryPO po);

}
