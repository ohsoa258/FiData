package com.fisk.mdm.map;

import com.fisk.mdm.dto.access.TableHistoryDTO;
import com.fisk.mdm.entity.TableHistoryPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableHistoryMap {
    TableHistoryMap INSTANCES = Mappers.getMapper(TableHistoryMap.class);
    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    TableHistoryPO dtoToPo(TableHistoryDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    TableHistoryDTO poToDto(TableHistoryPO po);
    /**
     * dto==>Po
     * @param dto
     * @return
     */
    List<TableHistoryPO> dtoListToPoList(List<TableHistoryDTO> dto);

    /**
     * poList==>DtoList
     * @param po
     * @return
     */
    List<TableHistoryDTO> poListToDtoList(List<TableHistoryPO> po);

}
