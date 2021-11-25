package com.fisk.dataaccess.map;

import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.datamodel.AppRegistrationDataDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
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
public interface AppRegistrationMap {

    AppRegistrationMap INSTANCES = Mappers.getMapper(AppRegistrationMap.class);

    /**
     * dto => po
     *
     * @param po source
     * @return target
     */
    AppRegistrationPO dtoToPo(AppRegistrationDTO po);

    /**
     * po => dto
     *
     * @param po po
     * @return dto
     */
    AppRegistrationDTO poToDto(AppRegistrationPO po);

    /**
     * list集合 po -> dto
     *
     * @param list source
     * @return target
     */
    List<AppRegistrationDTO> listPoToDto(List<AppRegistrationPO> list);

    /**
     * list集合 dto -> po
     *
     * @param list source
     * @return target
     */
    List<AppRegistrationPO> listDtoToPo(List<AppRegistrationDTO> list);

    /**
     * listPo==>DtoList
     * @param list
     * @return
     */
    List<AppRegistrationDataDTO> listPoToDtoList(List<AppRegistrationPO> list);

    /**
     * po -> dto
     *
     * @param po source
     * @return target
     */
    @Mappings({
            @Mapping(source = "appName", target = "businessName")
    })
    ChannelDataDTO poToChannelDataDto(AppRegistrationPO po);

    /**
     * list: po -> dto
     * @param list source
     * @return target
     */
    List<ChannelDataDTO> listPoToChannelDataDto(List<AppRegistrationPO> list);
}
