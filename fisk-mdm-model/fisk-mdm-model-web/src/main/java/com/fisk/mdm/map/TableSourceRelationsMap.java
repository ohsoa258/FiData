package com.fisk.mdm.map;

import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.mdm.entity.TableSourceRelationsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-22
 * @Description:
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableSourceRelationsMap {
    TableSourceRelationsMap INSTANCES = Mappers.getMapper(TableSourceRelationsMap.class);

    TableSourceRelationsPO dtoToPo(TableSourceRelationsDTO dto);

    List<TableSourceRelationsPO> dtoListToPoList(List<TableSourceRelationsDTO> dto);

    TableSourceRelationsDTO poToDto(TableSourceRelationsPO po);

    List<TableSourceRelationsDTO> poListToDtoList(List<TableSourceRelationsPO> dto);
}
