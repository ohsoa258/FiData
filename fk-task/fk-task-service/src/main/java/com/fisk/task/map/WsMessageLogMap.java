package com.fisk.task.map;


import com.fisk.task.entity.MessageLogPO;
import com.fisk.task.vo.WsMessageLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author gy
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WsMessageLogMap {

    WsMessageLogMap INSTANCES = Mappers.getMapper(WsMessageLogMap.class);

    /**
     * po => vo
     * @param po po
     * @return vo
     */
    @Mappings({
            @Mapping(source = "status.value", target = "status")
    })
    List<WsMessageLogVO> poToVo(List<MessageLogPO> po);

}
