package com.fisk.datagovernance.map.dataquality;

import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeEditDTO;
import com.fisk.datagovernance.entity.dataquality.NoticePO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author dick
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NoticeMap {

    NoticeMap INSTANCES = Mappers.getMapper(NoticeMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "noticeType.value", target = "noticeType"),
            @Mapping(source = "moduleState.value", target = "moduleState")
           // @Mapping(target = "componentNotificationDTOS", ignore = true)
    })
    NoticePO dtoToPo(NoticeDTO dto);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(source = "noticeType.value", target = "noticeType"),
            @Mapping(source = "moduleState.value", target = "moduleState")
            //@Mapping(target = "componentNotificationDTOS", ignore = true)
    })
    NoticePO dtoToPo_Edit(NoticeEditDTO dto);
}