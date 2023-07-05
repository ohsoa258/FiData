package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.dto.label.GlobalSearchDto;
import com.fisk.datamanagement.entity.GlossaryPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GlossaryMap {
    GlossaryMap INSTANCES = Mappers.getMapper(GlossaryMap.class);

    /**
     *
     * @param po
     * @return
     */
    @Mapping(target = "type" ,constant = "GLOSSARY")
    @Mapping(source = "glossaryLibraryId",target = "pid")
    GlobalSearchDto poToglobalSearchDto(GlossaryPO po);

    /**
     * poList ==> GlobalSearchDtoList
     * @param poList
     * @return
     */
    List<GlobalSearchDto> poToGlobalSearchDtoList(List<GlossaryPO> poList);

    /**
     *po ==>dto
     * @param po
     * @return
     */
    GlossaryDTO poToDto(GlossaryPO po);

    /**
     * poList ==> dtoList
     * @param po
     * @return
     */
    List<GlossaryDTO> poToDtoList(List<GlossaryPO> po);
}
