package com.fisk.mdm.map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attribute.AttributePageDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author chenYa
 */
@Mapper( uses = { TypeConversionUtils.class } , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttributeMap {
    AttributeMap INSTANCES = Mappers.getMapper(AttributeMap.class);

    /**
     * dto => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "enableAttributeLog" ,target = "enableAttributeLog"),
            @Mapping(source = "enableReadonly" ,target = "enableReadonly"),
            @Mapping(source = "enableRequired" ,target = "enableRequired"),
            @Mapping(source = "mdmType" , target = "mdmType"),
            @Mapping(source = "dataType" , target = "dataType"),
            @Mapping(source = "status" ,target = "status")
    })
    AttributePO dtoToPo(AttributeDTO dto);

    /**
     * po => vo
     * @param po
     * @return
     */
    @Mappings({
            @Mapping(source = "enableAttributeLog" ,target = "enableAttributeLog"),
            @Mapping(source = "enableReadonly" ,target = "enableReadonly"),
            @Mapping(source = "enableRequired" ,target = "enableRequired"),
            @Mapping(source = "mdmType" , target = "mdmType"),
            @Mapping(source = "dataType" , target = "dataType"),
            @Mapping(source = "status" ,target = "status")
    })
    AttributeVO poToVo(AttributePO po);

    /**
     * updateDTO => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "enableAttributeLog" ,target = "enableAttributeLog"),
            @Mapping(source = "enableReadonly" ,target = "enableReadonly"),
            @Mapping(source = "enableRequired" ,target = "enableRequired"),
            @Mapping(source = "mdmType" , target = "mdmType"),
            @Mapping(source = "dataType" , target = "dataType"),
            @Mapping(source = "status" ,target = "status")
    })
    AttributePO updateDtoToPo(AttributeUpdateDTO dto);

    /**
     * po => dto
     * @param po
     * @return
     */
    @Mappings({
            @Mapping(source = "status" ,target = "status"),
            @Mapping(source = "syncStatus" ,target = "syncStatus")
    })
    AttributeInfoDTO poToInfoDto(AttributePO po);

    /**
     * poList => DtoList
     *
     * @param list 列表
     * @return {@link List}<{@link AttributeInfoDTO}>
     */
    @Mappings({
            @Mapping(source = "enableAttributeLog" ,target = "enableAttributeLog"),
            @Mapping(source = "enableReadonly" ,target = "enableReadonly"),
            @Mapping(source = "enableRequired" ,target = "enableRequired"),
            @Mapping(source = "mdmType" , target = "mdmType"),
            @Mapping(source = "dataType" , target = "dataType"),
            @Mapping(source = "status" ,target = "status")
    })
    List<AttributeInfoDTO> poToDtoList(List<AttributePO> list);

    /**
     * entityPo => EntityInfoVO
     *
     * @param entityPo 实体po
     * @return {@link EntityInfoVO}
     */
    EntityInfoVO poToEntityVo(EntityPO entityPo);

    /**
     * vo => pageDto
     *
     * @param voPage voPage
     * @return {@link Page}<{@link AttributePageDTO}>
     */
    Page<AttributePageDTO> voToPageDtoPage(Page<AttributeVO> voPage);

    /**
     * pageDto => vo
     *
     * @param pageDTOPage pageDTOPage
     * @return {@link Page}<{@link AttributePO}>
     */
    Page<AttributePO> pageDtoToPoPage(Page<AttributePageDTO> pageDTOPage);

    /**
     * poPage => voPage
     *
     * @param poPage poPage
     * @return {@link Page}<{@link AttributeVO}>
     */
    Page<AttributeVO> poToVoPage(Page<AttributePO> poPage);


    /**
     * po => infoDto
     *
     * @param po po
     * @return {@link List}<{@link AttributeInfoDTO}>
     */
    List<AttributeInfoDTO> poToVoList(List<AttributePO> po);
}



