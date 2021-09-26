package com.fisk.datagovern.map;

import com.fisk.datagovern.dto.category.CategoryDTO;
import com.fisk.datagovern.entity.CategoryPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

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

}
