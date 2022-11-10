package com.fisk.datamanagement.map;

import com.fisk.datamanagement.dto.businessmetadataconfig.BusinessMetadataConfigDTO;
import com.fisk.datamanagement.entity.BusinessMetadataConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessMetadataConfigMap {

    BusinessMetadataConfigMap INSTANCES = Mappers.getMapper(BusinessMetadataConfigMap.class);

    /**
     * poList==>DtoList
     *
     * @param poList
     * @return
     */
    List<BusinessMetadataConfigDTO> poListToDtoList(List<BusinessMetadataConfigPO> poList);

}
