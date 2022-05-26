package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppDataSourceMap {

    AppDataSourceMap INSTANCES = Mappers.getMapper(AppDataSourceMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    AppDataSourcePO dtoToPo(AppDataSourceDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    AppDataSourceDTO poToDto(AppDataSourcePO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<AppDataSourceDTO> listPoToDto(List<AppDataSourcePO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<AppDataSourcePO> listDtoToPo(List<AppDataSourceDTO> list);


}
