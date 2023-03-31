package com.fisk.system.map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.roleinfo.RolePowerDTO;
import com.fisk.system.entity.RoleInfoPO;
import com.fisk.system.vo.roleinfo.RoleInfoVo;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleInfoMap {
    RoleInfoMap INSTANCES = Mappers.getMapper(RoleInfoMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    RoleInfoPO dtoToPo(RoleInfoDTO dto);

    /**
     * dto => po
     *
     * @param po
     * @return target
     */
    RoleInfoDTO poToDto(RoleInfoPO po);

    /**
     * dtoList => poList
     *
     * @param poList
     * @return target
     */
    List<RoleInfoDTO> poListToDtoList(List<RoleInfoPO> poList);
    /**
     * list<po> => list<dto>
     *
     * @param po
     * @return target
     */
    List<RoleInfoDTO> poToDtos(List<RoleInfoPO> po);
    /**
     * Page<po> => Page<dto>
     *
     * @param po
     * @return target
     */
    Page<RolePowerDTO> poToPageDto(IPage<RoleInfoPO> po);

}
