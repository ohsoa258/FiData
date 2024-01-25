package com.fisk.dataservice.map;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.dataservice.dto.datasource.DataSourceConDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConEditDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConfigInfoDTO;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.util.TypeConversionUtils;
import com.fisk.system.dto.datasource.DataSourceDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author dick
 */
@Mapper(uses = { TypeConversionUtils.class } ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DataSourceConMap {

    DataSourceConMap INSTANCES = Mappers.getMapper(DataSourceConMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "conType.value", target = "conType"),
            @Mapping(source = "datasourceType.value", target = "datasourceType")
    })
    DataSourceConPO dtoToPo(DataSourceConDTO dto);


    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "conType.value", target = "conType"),
            @Mapping(source = "datasourceType.value", target = "datasourceType")
    })
    void editDtoToPo(DataSourceConEditDTO dto, @MappingTarget DataSourceConPO po);

    /**
     * voList==>DtoInfo
     *
     * @param voList
     * @return
     */
    List<DataSourceConfigInfoDTO> voListToDtoInfo(List<DataSourceDTO> voList);

//    @Mappings({
//            @Mapping(source = "conType", target = "conType"),
//    })
//    DataSourceDTO poToDto(DataSourceConPO po);
}
