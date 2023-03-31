package com.fisk.mdm.map;

import com.fisk.mdm.dto.process.ProcessInfoDTO;
import com.fisk.mdm.entity.ProcessInfoPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.process.ProcessInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessInfoMap {
    ProcessInfoMap INSTANCES = Mappers.getMapper(ProcessInfoMap.class);

    @Mappings({
            @Mapping(source = "exceptionhandling" ,target = "exceptionhandling"),
            @Mapping(source = "autoapproal" ,target = "autoapproal"),
            @Mapping(source = "enable" ,target = "enable")
    })
    ProcessInfoVO poToVo(ProcessInfoPO po);
    @Mappings({
            @Mapping(source = "exceptionhandling" ,target = "exceptionhandling"),
            @Mapping(source = "autoapproal" ,target = "autoapproal"),
            @Mapping(source = "enable" ,target = "enable")
    })
    ProcessInfoPO dtoToPo(ProcessInfoDTO po);
}
