package com.fisk.mdm.map;

import com.fisk.mdm.entity.ProcessApplyPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.process.ProcessApplyVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-04-06
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessApplyMap {
    ProcessApplyMap INSTANCES = Mappers.getMapper(ProcessApplyMap.class);

    /**
     * po => vo
     * @param po
     * @return
     */
    @Mappings({
            @Mapping(source = "opreationstate.value", target = "opreationstate"),
            @Mapping(target = "applyId", source = "id")
    })
    ProcessApplyVO poToVo(ProcessApplyPO po);

    List<ProcessApplyVO> poListToVoList(List<ProcessApplyPO> poList);
}
