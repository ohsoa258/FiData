package com.fisk.mdm.map;

import com.fisk.mdm.dto.complextype.GeographyDTO;
import com.fisk.mdm.dto.complextype.GeographyDataDTO;
import com.fisk.mdm.vo.complextype.GeographyVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ComplexTypeMap {
    ComplexTypeMap INSTANCES = Mappers.getMapper(ComplexTypeMap.class);

    /**
     * dto ==> Vo
     *
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(source = "code", target = "code"),
            @Mapping(source = "mapType", target = "map_type"),
            @Mapping(source = "versionId", target = "fidata_version_id")
    })
    GeographyVO dtoToVo(GeographyDTO dto);

    /**
     * dto => dto
     * @param list
     * @return
     */
    @Mappings({
            @Mapping(source = "code", target = "code"),
            @Mapping(source = "map_type", target = "mapType"),
            @Mapping(source = "fidata_version_id", target = "versionId")
    })
    GeographyDTO dataToDto(GeographyDataDTO list);

    /**
     * dto => dto
     * @param list
     * @return
     */
    List<GeographyDTO> dataToDto(List<GeographyDataDTO> list);
}
