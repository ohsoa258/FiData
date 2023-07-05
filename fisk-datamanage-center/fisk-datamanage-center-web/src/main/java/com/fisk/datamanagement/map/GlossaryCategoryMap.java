package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.glossary.FirstGlossaryCategorySummaryDto;
import com.fisk.datamanagement.dto.label.GlobalSearchDto;
import com.fisk.datamanagement.entity.GlossaryLibraryPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JinXingWang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GlossaryCategoryMap {
    GlossaryCategoryMap INSTANCES = Mappers.getMapper(GlossaryCategoryMap.class);

    /**
     * GlossaryLibraryPO --> GlobalSearchDto
     * @param po
     * @return
     */
    @Mapping(target = "type" ,constant = "GLOSSARY_CATEGORY")
    GlobalSearchDto poToGlobalSearchDto(GlossaryLibraryPO po);

    /**
     * GlossaryLibraryPOList --> GlobalSearchDtoList
     * @param poList
     * @return
     */
    List<GlobalSearchDto> poToGlobalSearchDtoList(List<GlossaryLibraryPO> poList);

    /**
     * GlossaryLibraryPO --> FirstGlossaryCategorySummaryDto
     * @param po
     * @return
     */
    FirstGlossaryCategorySummaryDto poToFirstGlossaryCategorySummaryDtoList(GlossaryLibraryPO po);
}
