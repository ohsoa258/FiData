package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.ComponentsClassDTO;
import com.fisk.chartvisual.dto.ComponentsClassEditDTO;
import com.fisk.chartvisual.dto.ComponentsDTO;
import com.fisk.chartvisual.dto.ComponentsEditDTO;
import com.fisk.chartvisual.entity.ComponentsClassPO;
import com.fisk.chartvisual.entity.ComponentsPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 组件管理
 * @author WangYan
 * @date 2022/2/9 15:50
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ComponentsMap {

    ComponentsMap INSTANCES = Mappers.getMapper(ComponentsMap.class);

    /**
     * po => dto
     * @param po
     * @return
     */
    ComponentsClassDTO poToDto(ComponentsClassPO po);

    /**
     * dto => po
     * @param dto
     * @return
     */
    ComponentsClassPO classDtoToPo(ComponentsClassDTO dto);

    /**
     * dto => po
     * @param dto
     * @param uploadAddress
     * @return
     */
    @Mappings({
            @Mapping(source = "uploadAddress",target = "path")
    })
    ComponentsPO compDtoToPo(ComponentsDTO dto, String uploadAddress);

    /**
     * dto => po
     * @param dto
     * @return
     */
    ComponentsPO compEditDtoToPo(ComponentsEditDTO dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    ComponentsClassPO compClassEditDtoToPo(ComponentsClassEditDTO dto);

    /**
     * po => dto
     * @param po
     * @return
     */
    List<ComponentsDTO> poToDtoList(List<ComponentsPO> po);
}
