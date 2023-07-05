package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.label.LabelDTO;
import com.fisk.datamanagement.dto.label.LabelDataDTO;
import com.fisk.datamanagement.dto.label.LabelInfoDTO;
import com.fisk.datamanagement.dto.label.GlobalSearchDto;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDTO;
import com.fisk.datamanagement.entity.LabelCategoryPO;
import com.fisk.datamanagement.entity.LabelPO;
    import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LabelMap {
    LabelMap INSTANCES = Mappers.getMapper(LabelMap.class);

    /**
     * dto==>po
     * @param dto
     * @return
     */
    LabelPO dtoToPo(LabelDTO dto);

    /**
     * pos=>dtos 不要问我为什么要放在这个地方，因为放在LabelCategoryMap会异常
     * @param po
     * @return
     */
    List<LabelCategoryDTO> categoryPosToDtos(List<LabelCategoryPO> po);

    /**
     * po==>dto
     * @param po
     * @return
     */
    LabelDTO poToDto(LabelPO po);

    /**
     * LabelDto ==> TermDto
     * @param dto
     * @return
     */
    @Mapping(source = "labelCnName",target = "name")
    @Mapping(target = "type",constant = "LABEL")
    @Mapping(source = "categoryId",target = "pid")
    GlobalSearchDto labelDtoToTermDt(LabelInfoDTO dto);


    /**
     * LabelDto ==> TermDto
     * @param dtos
     * @return
     */
    List<GlobalSearchDto> labelDtoToTermDtoList(List<LabelInfoDTO> dtos);


    LabelDataDTO poToLabelDataDto(LabelPO po);

    List<LabelDataDTO> poToLabelDataDtoList(List<LabelPO> po);
}
