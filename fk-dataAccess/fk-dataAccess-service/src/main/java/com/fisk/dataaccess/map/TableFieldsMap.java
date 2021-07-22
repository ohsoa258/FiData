package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableFieldsMap {

    TableFieldsMap INSTANCES = Mappers.getMapper(TableFieldsMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    TableFieldsPO dtoToPo(TableFieldsDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    TableFieldsDTO poToDto(TableFieldsPO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<TableFieldsDTO> listPoToDto(List<TableFieldsPO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableFieldsPO> listDtoToPo(List<TableFieldsDTO> list);


}
