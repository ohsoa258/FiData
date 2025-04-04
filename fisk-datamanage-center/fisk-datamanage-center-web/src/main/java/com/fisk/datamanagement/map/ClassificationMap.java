package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.businessclassification.FirstBusinessClassificationSummaryDto;
import com.fisk.datamanagement.dto.classification.ClassificationDefContentDTO;
import com.fisk.datamanagement.dto.classification.ClassificationTreeDTO;
import com.fisk.datamanagement.entity.BusinessClassificationPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClassificationMap {

    ClassificationMap INSTANCES = Mappers.getMapper(ClassificationMap.class);

    /**
     * po==>Dto
     *
     * @param po
     * @return
     */
    List<ClassificationTreeDTO> poListToDtoList(List<ClassificationDefContentDTO> po);

    /**
     * po==>Dto
     *
     * @param po
     * @return
     */
    ClassificationTreeDTO poToDto(ClassificationDefContentDTO po);


    /**
     * po ==> FirstBusinessClassificationSummaryDto
     * @param po
     * @return
     */
    FirstBusinessClassificationSummaryDto poToFirstBusinessClassificationSummaryDto(BusinessClassificationPO po);
}
