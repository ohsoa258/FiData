package com.fisk.datagovernance.map.datasecurity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoDropDTO;
import com.fisk.datagovernance.dto.datasecurity.usergroupinfo.UserGroupInfoPageDTO;
import com.fisk.datagovernance.entity.datasecurity.UserGroupInfoPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserGroupInfoMap {

    UserGroupInfoMap INSTANCES = Mappers.getMapper(UserGroupInfoMap.class);

    /**
     * dto==>Po
     * @param dto
     * @return
     */
    UserGroupInfoPO dtoToPo(UserGroupInfoDTO dto);

    /**
     * po==>Dto
     * @param po
     * @return
     */
    UserGroupInfoDTO poToDto(UserGroupInfoPO po);

    /**
     * poList==>DtoList
     * @param poList
     * @return
     */
    Page<UserGroupInfoPageDTO> poListToDtoList(IPage<UserGroupInfoPO> poList);

    /**
     * po==>DtoDrop
     * @param poList
     * @return
     */
    List<UserGroupInfoDropDTO> poToDtoDrop(List<UserGroupInfoPO> poList);


}
