package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDTO;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDataDTO;
import com.fisk.datamanagement.entity.CategoryPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LabelCategoryMap {
    LabelCategoryMap INSTANCES = Mappers.getMapper(LabelCategoryMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    CategoryPO dtoToPo(LabelCategoryDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    LabelCategoryDTO poToDto(CategoryPO po);

    /**
     * dataListPo==>Dto
     * @param po
     * @return
     */
    List<LabelCategoryDataDTO> dataListPoToDto(List<CategoryPO> po);

    /**
     * dataPo==>Dto
     * @param po
     * @return
     */
    LabelCategoryDataDTO dataPoToDto(CategoryPO po);

}
