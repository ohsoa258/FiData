package com.fisk.system.map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.system.dto.UserDTO;
import com.fisk.system.dto.UserPowerDTO;
import com.fisk.system.entity.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;


/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMap {
    UserMap INSTANCES = Mappers.getMapper(UserMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    UserPO dtoToPo(UserDTO dto);

    /**
     * po => dto
     *
     * @param po
     * @return target
     */
    UserDTO poToDto(UserPO po);

    /**
     * List<po> => List<dto>
     *
     * @param po
     * @return target
     */
    List<UserDTO> poToDtos(List<UserPO> po);

    /**
     * Page<po> => Page<dto>
     *
     * @param po
     * @return target
     */
    Page<UserPowerDTO> poToPageDto(IPage<UserPO> po);

}
