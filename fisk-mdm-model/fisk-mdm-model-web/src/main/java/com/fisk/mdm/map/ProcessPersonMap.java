package com.fisk.mdm.map;

import com.fisk.mdm.dto.process.ProcessPersonDTO;
import com.fisk.mdm.entity.ProcessPersonPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.process.ProcessPersonVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
@Mapper(uses = { TypeConversionUtils.class } ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessPersonMap {
    ProcessPersonMap INSTANCES = Mappers.getMapper(ProcessPersonMap.class);
    @Mappings({
            @Mapping(source = "type.value" ,target = "type"),
            @Mapping(source = "type.name" ,target = "typeName")
    })
    ProcessPersonVO poToVo(ProcessPersonPO po);

    List<ProcessPersonVO> poListToVoList(List<ProcessPersonPO> poList);

    List<ProcessPersonPO> dtoListToPoList(List<ProcessPersonDTO> poList);
}
