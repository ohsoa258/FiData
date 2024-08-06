package com.fisk.mdm.map;

import com.fisk.datamodel.dto.versionsql.VersionSqlDTO;
import com.fisk.mdm.entity.TableVersionSqlPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VersionSqlMap {

    VersionSqlMap INSTANCES = Mappers.getMapper(VersionSqlMap.class);

    /**
     * po -> dto
     *
     * @param po
     * @return
     */
    VersionSqlDTO poToDto(TableVersionSqlPO po);

    /**
     * pos -> dtos
     *
     * @param pos
     * @return
     */
    List<VersionSqlDTO> poListToDtoList(List<TableVersionSqlPO> pos);

    /**
     * dto -> po
     *
     * @param dto
     * @return
     */
    TableVersionSqlPO dtoToPo(VersionSqlDTO dto);

    /**
     * pos -> dtos
     *
     * @param pos
     * @return
     */
    List<TableVersionSqlPO> dtoListToPoList(List<VersionSqlDTO> pos);

}
