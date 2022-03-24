package com.fisk.datamanagement.map;

import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceFieldDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamodel.dto.tableconfig.SourceFieldDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetadataMapAtlasMap {

    MetadataMapAtlasMap INSTANCES = Mappers.getMapper(MetadataMapAtlasMap.class);

    /**
     * dataAccess==>sourceData
     * @param dto
     * @return
     */
    SourceTableDTO dtoToDto(DataAccessSourceTableDTO dto);

    /**
     * field==>sourceField
     * @param dto
     * @return
     */
    List<SourceFieldDTO> fieldToDto(List<DataAccessSourceFieldDTO> dto);

}
