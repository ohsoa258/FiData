package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.RowSecurityConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.RowSecurityConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RowSecurityConfigMap {

    RowSecurityConfigMap INSTANCES = Mappers.getMapper(RowSecurityConfigMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    RowSecurityConfigPO dtoToPo(RowSecurityConfigDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    RowSecurityConfigDTO poToDto(RowSecurityConfigPO dto);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<RowSecurityConfigDTO> listPoToDto(List<RowSecurityConfigPO> list);

    /**
     * list: dto -> po
     *
     * @param list source
     * @return target
     */
    List<RowSecurityConfigPO> listDtoToPo(List<RowSecurityConfigDTO> list);
}
