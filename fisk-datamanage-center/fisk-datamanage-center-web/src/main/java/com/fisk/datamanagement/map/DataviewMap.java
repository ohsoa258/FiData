package com.fisk.datamanagement.map;

import com.fisk.datamanagement.entity.DataviewFilterPO;
import com.fisk.datamanagement.entity.DataviewPO;
import com.fisk.datamanagement.dto.dataview.DataViewAddDTO;
import com.fisk.datamanagement.dto.dataview.DataViewDTO;
import com.fisk.datamanagement.dto.dataview.DataViewEditDTO;
import com.fisk.datamanagement.dto.dataview.DataViewFilterDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/3 15:50
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,componentModel = "spring")
public interface DataviewMap {

    DataviewMap INSTANCES = Mappers.getMapper(DataviewMap.class);

    /**
     * po => dto
     * @param po
     * @return
     */
    DataViewDTO poToDto(DataviewPO po);

    /**
     * po => dto
     * @param po
     * @return
     */
    @Mappings(
            @Mapping(target = "dataviewId" ,ignore = true)
    )
    List<DataViewFilterDTO> filterPoToDto(List<DataviewFilterPO> po);

    /**
     * dto = po
     * @param dto
     * @return
     */
    DataviewPO dtoToPo(DataViewAddDTO dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    List<DataviewFilterPO> filterDtoToPo(List<DataViewFilterDTO> dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    DataviewPO dtoToPo(DataViewEditDTO dto);
}
