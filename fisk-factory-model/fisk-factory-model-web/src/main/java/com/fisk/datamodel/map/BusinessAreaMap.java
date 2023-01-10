package com.fisk.datamodel.map;

import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaDTO;
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
public interface BusinessAreaMap {

    BusinessAreaMap INSTANCES = Mappers.getMapper(BusinessAreaMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    BusinessAreaPO dtoToPo(BusinessAreaDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    BusinessAreaDTO poToDto(BusinessAreaPO po);

    /**
     * po==>BusinessAreaInfo
     *
     * @param po
     * @return
     */
    @Mappings({
            @Mapping(source = "businessName", target = "name"),
            @Mapping(source = "businessDes", target = "appDes")
    })
    AppBusinessInfoDTO poToBusinessAreaInfo(BusinessAreaPO po);

    /**
     * poList==>BusinessAreaInfo
     *
     * @param poList
     * @return
     */
    List<AppBusinessInfoDTO> poListToBusinessAreaInfo(List<BusinessAreaPO> poList);
}
