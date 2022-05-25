package com.fisk.mdm.map;

import com.fisk.mdm.dto.viwGroup.UpdateViwGroupDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDetailsDTO;
import com.fisk.mdm.entity.ViwGroupDetailsPO;
import com.fisk.mdm.entity.ViwGroupPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.viwGroup.ViewGroupDropDownVO;
import com.fisk.mdm.vo.viwGroup.ViwGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author chenYa
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ViwGroupMap {

    ViwGroupMap INSTANCES = Mappers.getMapper(ViwGroupMap.class);

    /**
     * po => vo
     * @param po
     * @return
     */
    ViwGroupVO groupPoToVo(ViwGroupPO po);

    /**
     * dto => po
     * @param dto
     * @return
     */
    ViwGroupPO groupDtoToPo(ViwGroupDTO dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    ViwGroupPO groupUpdateDtoToPo(UpdateViwGroupDTO dto);

    /**
     *
     * @param dto
     * @return
     */
    ViwGroupDetailsPO detailsDtoToDto(ViwGroupDetailsDTO dto);

    /**
     * po => dto list
     *
     * @param poList
     * @return
     */
    List<ViwGroupDetailsDTO> detailsPoToDtoList(List<ViwGroupDetailsPO> poList);

    /**
     * poList==>DropDownVo
     *
     * @param poList
     * @return
     */
    List<ViewGroupDropDownVO> poListToDropDownVo(List<ViwGroupPO> poList);

}



