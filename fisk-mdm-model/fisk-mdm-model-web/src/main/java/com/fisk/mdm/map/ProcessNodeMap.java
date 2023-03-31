package com.fisk.mdm.map;

import com.fisk.mdm.dto.process.ProcessNodeDTO;
import com.fisk.mdm.entity.ProcessNodePO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.process.ProcessNodeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description:
 */
@Mapper(uses = { TypeConversionUtils.class } ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessNodeMap {
    ProcessNodeMap INSTANCES = Mappers.getMapper(ProcessNodeMap.class);

    @Mappings({
            @Mapping(source = "settype.value" ,target = "settype"),
            @Mapping(source = "settype.name" ,target = "typeName")
    })
    ProcessNodeVO poToVo(ProcessNodePO po);
    List<ProcessNodeVO> poListToVoList(List<ProcessNodePO> poList);

    List<ProcessNodePO> dtoListToPoList(List<ProcessNodeDTO> poList);
}
