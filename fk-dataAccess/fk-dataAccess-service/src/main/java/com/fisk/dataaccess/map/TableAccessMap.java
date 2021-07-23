package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.TableAccessDTO;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableAccessMap {

    TableAccessMap INSTANCES = Mappers.getMapper(TableAccessMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    TableAccessPO dtoToPo(TableAccessDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    TableAccessDTO poToDto(TableAccessPO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    TableAccessNonDTO poToDtoNon(TableAccessPO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<TableAccessDTO> listPoToDto(List<TableAccessPO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<TableAccessPO> listDtoToPo(List<TableAccessDTO> list);


}
