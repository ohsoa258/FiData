package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.email.EmailGroupDTO;
import com.fisk.datamanagement.entity.EmailGroupPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmailGroupMap {

    EmailGroupMap INSTANCES = Mappers.getMapper(EmailGroupMap.class);

    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    EmailGroupPO dtoToPo(EmailGroupDTO dto);

    EmailGroupDTO poToDto(EmailGroupPO po);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    List<EmailGroupPO> dtoListToPoList(List<EmailGroupDTO> dto);

    List<EmailGroupDTO> poListToDtoList(List<EmailGroupPO> po);

}
