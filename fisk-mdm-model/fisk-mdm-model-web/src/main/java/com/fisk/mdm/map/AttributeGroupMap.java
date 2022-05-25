package com.fisk.mdm.map;

import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDetailsDTO;
import com.fisk.mdm.dto.attributeGroup.UpdateAttributeGroupDTO;
import com.fisk.mdm.entity.AttributeGroupDetailsPO;
import com.fisk.mdm.entity.AttributeGroupPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupDropDownVO;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author chenYa
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttributeGroupMap {

    AttributeGroupMap INSTANCES = Mappers.getMapper(AttributeGroupMap.class);

    /**
     * po => vo
     * @param po
     * @return
     */
    AttributeGroupVO groupPoToVo(AttributeGroupPO po);

    /**
     * dto => po
     * @param dto
     * @return
     */
    AttributeGroupPO groupDtoToPo(UpdateAttributeGroupDTO dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    AttributeGroupDetailsPO detailsDtoToDto(AttributeGroupDetailsDTO dto);

    /**
     * 属性组创建
     * @param dto
     * @return
     */
    AttributeGroupPO groupDtoToPo(AttributeGroupDTO dto);

    /**
     * po => dto
     * @param po
     * @return
     */
    AttributeGroupDetailsDTO detailsPoToDto(AttributeGroupDetailsPO po);

    /**
     * po => dto
     *
     * @param poList
     * @return
     */
    List<AttributeGroupDetailsDTO> detailsPoToVoList(List<AttributeGroupDetailsPO> poList);

    /**
     * groupListPo==>VoList
     *
     * @param list
     * @return
     */
    List<AttributeGroupDropDownVO> groupListPoToVoList(List<AttributeGroupPO> list);

    /**
     * po==>Dto
     *
     * @param po
     * @return
     */
    AttributeGroupDTO poToDto(AttributeGroupPO po);

}



