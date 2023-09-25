package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tableapi.TableApiParameterDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-09-12
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableApiParameterMap {
    TableApiParameterMap INSTANCES = Mappers.getMapper(TableApiParameterMap.class);

    /**
     *  dto -> po
     *
     * @param  dto source
     * @return target
     */
    TableApiParameterPO dtoToPo(TableApiParameterDTO dto);
    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableApiParameterPO> listDtoToPo(List<TableApiParameterDTO> list);
    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<TableApiParameterDTO> listPoToDto(List<TableApiParameterPO> list);
}
