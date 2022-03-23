package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.TableHistoryDTO;
import com.fisk.dataaccess.entity.TableHistoryPO;
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
