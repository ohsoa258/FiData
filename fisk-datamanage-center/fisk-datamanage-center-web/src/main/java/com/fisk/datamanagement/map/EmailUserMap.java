package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.email.EmailUserDTO;
import com.fisk.datamanagement.entity.EmailUserPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmailUserMap {


    EmailUserMap INSTANCES = Mappers.getMapper(EmailUserMap.class);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    EmailUserPO dtoToPo(EmailUserDTO dto);

    EmailUserDTO poToDto(EmailUserPO po);

    List<EmailUserPO> dtoListToPoList(List<EmailUserDTO> dto);

    List<EmailUserDTO> poListToDtoList(List<EmailUserPO> po);

}
