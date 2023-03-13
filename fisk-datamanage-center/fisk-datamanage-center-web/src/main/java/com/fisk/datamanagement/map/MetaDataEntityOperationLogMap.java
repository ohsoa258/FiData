package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.entity.MetaDataEntityOperationLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 10:51
 * @description
 */

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetaDataEntityOperationLogMap {
    MetaDataEntityOperationLogMap INSTANCES = Mappers.getMapper(MetaDataEntityOperationLogMap.class);

    /**
     *  po==>dto
     * @param logPO
     * @return
     */
    List<MetaDataEntityOperationLogDTO> logPoToDto(List<MetaDataEntityOperationLogPO> logPO);

    /**
     *  dto==>po
     * @param logDto
     * @return
     */
    MetaDataEntityOperationLogPO logDtoToPo(MetaDataEntityOperationLogDTO logDto);
}
