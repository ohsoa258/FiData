package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.RowfilterConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.RowfilterConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RowFilterConfigMap {

    RowFilterConfigMap INSTANCES = Mappers.getMapper(RowFilterConfigMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    RowfilterConfigPO dtoToPo(RowfilterConfigDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    RowfilterConfigDTO poToDto(RowfilterConfigPO dto);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<RowfilterConfigDTO> listPoToDto(List<RowfilterConfigPO> list);

    /**
     * list: dto -> po
     *
     * @param list source
     * @return target
     */
    List<RowfilterConfigPO> listDtoToPo(List<RowfilterConfigDTO> list);
}
