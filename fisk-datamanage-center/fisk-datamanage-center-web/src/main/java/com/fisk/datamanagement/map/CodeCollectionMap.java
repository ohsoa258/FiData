package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.DataSet.CodeCollectionDTO;
import com.fisk.datamanagement.entity.CodeCollectionPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-08-01
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CodeCollectionMap {
    CodeCollectionMap INSTANCES = Mappers.getMapper(CodeCollectionMap.class);



    CodeCollectionPO dtoToPo(CodeCollectionDTO dto);

    CodeCollectionDTO poToDTO(CodeCollectionPO dto);

    List<CodeCollectionDTO> poListToDTOList(List<CodeCollectionPO> dto);
}
