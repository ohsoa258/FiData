package com.fisk.system.map;
import com.fisk.system.dto.emailserver.EmailServerDTO;
import com.fisk.system.dto.emailserver.EmailServerEditDTO;
import com.fisk.system.entity.EmailServerPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmailServerMap {

    EmailServerMap INSTANCES = Mappers.getMapper(EmailServerMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "emailServerType.value", target = "emailServerType")
    })
    EmailServerPO dtoToPo(EmailServerDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "emailServerType.value", target = "emailServerType")
    })
    EmailServerPO dtoToPo_Edit(EmailServerEditDTO dto);
}