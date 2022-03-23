package com.fisk.task.map;

import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.entity.NifiStagePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author cfk
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NifiStageMap {
    NifiStageMap INSTANCES = Mappers.getMapper(NifiStageMap.class);

    /**
     * dto => po
     * @param dto source
     * @return target
     */
    NifiStagePO dtoToPo(NifiStageDTO dto);

    /**
     * po => dto
     * @param po source
     * @return target
     */
    NifiStageDTO poToDto(NifiStagePO po);

    /**
     * list集合 dto -> po
     *
     * @param list list
     * @return target
     */
    List<NifiStagePO> listDtoToPo(List<NifiStageDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list list
     * @return target
     */
    List<NifiStageDTO> listPoToDto(List<NifiStagePO> list);
}
