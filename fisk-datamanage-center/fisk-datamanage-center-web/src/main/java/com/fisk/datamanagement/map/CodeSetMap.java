package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.DataSet.CodeSetDTO;
import com.fisk.datamanagement.entity.CodeSetPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-01-30
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CodeSetMap {

    CodeSetMap INSTANCES = Mappers.getMapper(CodeSetMap.class);



    CodeSetPO dtoToPo(CodeSetDTO dto);

    CodeSetDTO poToDTO(CodeSetPO dto);

    List<CodeSetDTO> poListToDTOList(List<CodeSetPO> dto);
}
