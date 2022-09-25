package com.fisk.dataaccess.map;

import com.fisk.common.service.flinkupload.dto.FlinkUploadParameterDTO;
import com.fisk.dataaccess.dto.FlinkConfigDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FlinkParameterMap {

    FlinkParameterMap INSTANCES = Mappers.getMapper(FlinkParameterMap.class);

    /**
     * dto==>Dto
     *
     * @param dto
     * @return
     */
    FlinkUploadParameterDTO dtoToDto(FlinkConfigDTO dto);

}
