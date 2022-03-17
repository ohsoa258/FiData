package com.fisk.task.map;

import com.fisk.task.dto.pipeline.PipelineTableLogDTO;
import com.fisk.task.entity.PipelineTableLogPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author cfk
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PipelineTableLogMap {
    PipelineTableLogMap INSTANCES = Mappers.getMapper(PipelineTableLogMap.class);

    /**
     * dto => po
     * @param dto source
     * @return target
     */
    PipelineTableLogPO dtoToPo(PipelineTableLogDTO dto);

    /**
     * po => dto
     * @param po source
     * @return target
     */
    PipelineTableLogDTO poToDto(PipelineTableLogPO po);

    /**
     * list集合 dto -> po
     *
     * @param list list
     * @return target
     */
    List<PipelineTableLogPO> listDtoToPo(List<PipelineTableLogDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list list
     * @return target
     */
    List<PipelineTableLogDTO> listPoToDto(List<PipelineTableLogPO> list);

}
