package com.fisk.dataservice.map;
import com.fisk.dataservice.dto.tableapi.TableApiAuthRequestDTO;
import com.fisk.dataservice.entity.TableApiAuthRequestPO;
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
public interface TableApiAuthRequestMap {
    TableApiAuthRequestMap INSTANCES = Mappers.getMapper(TableApiAuthRequestMap.class);
    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableApiAuthRequestPO> listDtoToPo(List<TableApiAuthRequestDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<TableApiAuthRequestDTO> listPoToDto(List<TableApiAuthRequestPO> list);
}
