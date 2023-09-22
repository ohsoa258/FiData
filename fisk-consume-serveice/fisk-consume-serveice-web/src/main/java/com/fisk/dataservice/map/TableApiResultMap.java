package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tableapi.TableApiResultDTO;
import com.fisk.dataservice.entity.TableApiResultPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-09-11
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableApiResultMap {
    TableApiResultMap INSTANCES = Mappers.getMapper(TableApiResultMap.class);

    TableApiResultPO dtoToPo(TableApiResultDTO dto);
    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableApiResultPO> listDtoToPo(List<TableApiResultDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<TableApiResultDTO> listPoToDto(List<TableApiResultPO> list);
}
