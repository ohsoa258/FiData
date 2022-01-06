package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceFieldDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataAccessMap {

    DataAccessMap INSTANCES = Mappers.getMapper(DataAccessMap.class);

    /**
     * po => dto
     *
     * @param po source
     * @return target
     */
    DataAccessSourceTableDTO tablePoToDto(TableAccessPO po);

    /**
     * field: po => dto
     *
     * @param po source
     * @return target
     */
    @Mappings({
            @Mapping(source = "isPrimarykey", target = "primaryKey")
    })
    DataAccessSourceFieldDTO fieldPoToDto(TableFieldsPO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<DataAccessSourceTableDTO> tableListPoToDto(List<TableAccessPO> list);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<DataAccessSourceFieldDTO> fieldListPoToDto(List<TableFieldsPO> list);

}
