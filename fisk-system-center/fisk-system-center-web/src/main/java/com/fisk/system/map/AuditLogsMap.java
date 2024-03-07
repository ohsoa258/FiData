package com.fisk.system.map;

import com.fisk.system.dto.auditlogs.AuditLogsDTO;
import com.fisk.system.entity.AuditLogsPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuditLogsMap {

    AuditLogsMap INSTANCES = Mappers.getMapper(AuditLogsMap.class);


    AuditLogsDTO poToDto(AuditLogsPO po);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    AuditLogsPO dtoToPo(AuditLogsDTO dto);


}
