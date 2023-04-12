package com.fisk.dataservice.map;

import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.entity.TableFieldPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableFieldMap {

    TableFieldMap INSTANCES = Mappers.getMapper(TableFieldMap.class);

    /**
     * dtoList==>PoList
     *
     * @param dtoList
     * @return
     */
    List<TableFieldPO> dtoListToPoList(List<TableFieldDTO> dtoList);

    /**
     * poList==>DtoList
     *
     * @param poList
     * @return
     */
    List<TableFieldDTO> poListToDtoList(List<TableFieldPO> poList);

}
