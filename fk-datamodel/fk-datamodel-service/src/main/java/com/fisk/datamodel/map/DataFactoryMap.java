package com.fisk.datamodel.map;

import com.fisk.dataaccess.dto.datafactory.TableIdAndNameDTO;
import com.fisk.dataaccess.vo.datafactory.TableIdAndNameVO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datamodel.dto.BusinessAreaDTO;
import com.fisk.datamodel.dto.dimension.DimensionTabDTO;
import com.fisk.datamodel.dto.fact.FactTabDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
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
public interface DataFactoryMap {

    DataFactoryMap INSTANCES = Mappers.getMapper(DataFactoryMap.class);

    /**
     * list集合 dto -> po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "dimensionCnName",target = "tableName")
    })
    ChannelDataDTO tableDtoToPo(DimensionTabDTO dto);

    List<ChannelDataDTO> tableDtosToPos(List<DimensionTabDTO> list);

    /**
     * list集合 dto -> po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "factTableCnName",target = "tableName")
    })
    ChannelDataDTO tablesDtoToPo(FactTabDTO dto);
    List<ChannelDataDTO> tablesDtosToPos(List<FactTabDTO> list);

}
