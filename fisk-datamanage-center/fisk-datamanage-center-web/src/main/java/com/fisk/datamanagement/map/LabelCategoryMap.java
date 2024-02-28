package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.label.GlobalSearchDto;
import com.fisk.datamanagement.dto.labelcategory.FirstLabelCategorySummaryDto;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDTO;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDataDTO;
import com.fisk.datamanagement.dto.search.EntitiesDTO;
import com.fisk.datamanagement.entity.LabelCategoryPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
    LabelCategoryPO dtoToPo(LabelCategoryDTO dto);

    /**
     * po==>dto
     * @param po
     * @return
     */
    LabelCategoryDTO poToDto(LabelCategoryPO po);

    /**
     * dataListPo==>Dto
     * @param po
     * @return
     */
    List<LabelCategoryDataDTO> dataListPoToDto(List<LabelCategoryPO> po);

    /**
     * dataPo==>Dto
     * @param po
     * @return
     */
    LabelCategoryDataDTO dataPoToDto(LabelCategoryPO po);

    /**
     * LabelDto ==> TermDto
     * @param dto
     * @return
     */
    @Mapping(source = "categoryCnName",target = "name")
    @Mapping(target = "type",constant = "LABEL_CATEGORY")
    GlobalSearchDto labelCategoryDtoToTermDto(LabelCategoryDTO dto);

    /**
     * LabelDtos ==> TermDtos
     * @param dtos
     * @return
     */
    List<GlobalSearchDto> labelCategoryDtoToTermDtoList(List<LabelCategoryDTO> dtos);


    /**
     *  po ==> FirstLabelCategorySummaryDto
     * @param po
     * @return
     */
    FirstLabelCategorySummaryDto poToFirstLabelCategorySummaryDto(LabelCategoryPO po);


}
