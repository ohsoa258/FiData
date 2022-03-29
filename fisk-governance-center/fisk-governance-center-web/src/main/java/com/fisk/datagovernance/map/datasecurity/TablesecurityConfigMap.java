package com.fisk.datagovernance.map.datasecurity;

import com.fisk.datagovernance.dto.datasecurity.TablesecurityConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.TablesecurityConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TablesecurityConfigMap {

    TablesecurityConfigMap INSTANCES = Mappers.getMapper(TablesecurityConfigMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    TablesecurityConfigPO dtoToPo(TablesecurityConfigDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    TablesecurityConfigDTO poToDto(TablesecurityConfigPO dto);

    /**
     * list: po -> dto
     *
     * @param list source
     * @return target
     */
    List<TablesecurityConfigDTO> listPoToDto(List<TablesecurityConfigPO> list);

    /**
     * list: dto -> po
     *
     * @param list source
     * @return target
     */
    List<TablesecurityConfigPO> listDtoToPo(List<TablesecurityConfigDTO> list);
}
