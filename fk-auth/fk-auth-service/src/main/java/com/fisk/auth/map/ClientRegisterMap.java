package com.fisk.auth.map;

import com.fisk.auth.dto.clientregister.ClientRegisterDTO;
import com.fisk.auth.entity.ClientRegisterPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/17 14:51
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientRegisterMap {
    ClientRegisterMap INSTANCES = Mappers.getMapper(ClientRegisterMap.class);

    /**
     * po -> dto
     *
     * @param po po
     * @return dto
     */
    ClientRegisterDTO poToDto(ClientRegisterPO po);

    /**
     * dto -> po
     *
     * @param dto dto
     * @return po
     */
    ClientRegisterPO dtoToPo(ClientRegisterDTO dto);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<ClientRegisterDTO> listPoToDto(List<ClientRegisterPO> list);

    /**
     * list: dto -> po
     *
     * @param list source
     * @return target
     */
    List<ClientRegisterPO> listDtoToPo(List<ClientRegisterDTO> list);
}
