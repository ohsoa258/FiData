package com.fisk.mdm.map;

import com.fisk.mdm.dto.attributelog.AttributeLogDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogSaveDTO;
import com.fisk.mdm.entity.AttributeLogPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/6/14 15:06
 * @Version 1.0
 */
@Mapper(uses = { TypeConversionUtils.class },nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttributeLogMap {

    AttributeLogMap INSTANCES = Mappers.getMapper(AttributeLogMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    AttributeLogPO dtoToPo(AttributeLogSaveDTO dto);

    /**
     * po => dto
     * @param list
     * @return
     */
    List<AttributeLogDTO> logPoToDto(List<AttributeLogPO> list);
}
