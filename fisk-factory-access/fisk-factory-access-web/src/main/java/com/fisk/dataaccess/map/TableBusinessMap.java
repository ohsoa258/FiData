package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.dataaccess.entity.TableBusinessPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableBusinessMap {

    TableBusinessMap INSTANCES = Mappers.getMapper(TableBusinessMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    TableBusinessPO dtoToPo(TableBusinessDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    TableBusinessDTO poToDto(TableBusinessPO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<TableBusinessDTO> listPoToDto(List<TableBusinessPO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableBusinessPO> listDtoToPo(List<TableBusinessDTO> list);
}
