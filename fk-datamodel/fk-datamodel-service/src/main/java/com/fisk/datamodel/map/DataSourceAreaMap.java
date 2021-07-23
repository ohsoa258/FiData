package com.fisk.datamodel.map;

import com.fisk.datamodel.dto.DataSourceAreaDTO;
import com.fisk.datamodel.entity.DataSourceAreaPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataSourceAreaMap {

    DataSourceAreaMap INSTANCES = Mappers.getMapper(DataSourceAreaMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    DataSourceAreaPO dtoToPo(DataSourceAreaDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    DataSourceAreaDTO poToDto(DataSourceAreaPO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<DataSourceAreaDTO> listPoToDto(List<DataSourceAreaPO> list);
}
