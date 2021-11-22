package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.TableSyncmodeDTO;
import com.fisk.dataaccess.entity.TableSyncmodePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableSyncModeMap {

    TableSyncModeMap INSTANCES = Mappers.getMapper(TableSyncModeMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    TableSyncmodePO dtoToPo(TableSyncmodeDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    TableSyncmodeDTO poToDto(TableSyncmodePO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<TableSyncmodeDTO> listPoToDto(List<TableSyncmodePO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableSyncmodePO> listDtoToPo(List<TableSyncmodeDTO> list);


}
