package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.DatamaskingConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.DatamaskingConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DatamaskingConfigMap {

    DatamaskingConfigMap INSTANCES = Mappers.getMapper(DatamaskingConfigMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    DatamaskingConfigPO dtoToPo(DatamaskingConfigDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    DatamaskingConfigDTO poToDto(DatamaskingConfigPO dto);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<DatamaskingConfigDTO> listPoToDto(List<DatamaskingConfigPO> list);

    /**
     * list: dto -> po
     *
     * @param list source
     * @return target
     */
    List<DatamaskingConfigPO> listDtoToPo(List<DatamaskingConfigDTO> list);
}
