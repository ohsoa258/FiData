package com.fisk.datafactory.map;

import com.fisk.datafactory.dto.customworkflowdetail.ChildrenWorkflowDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Lock
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NifiCustomWorkflowDetailMap {

    NifiCustomWorkflowDetailMap INSTANCES = Mappers.getMapper(NifiCustomWorkflowDetailMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    NifiCustomWorkflowDetailPO dtoToPo(NifiCustomWorkflowDetailDTO dto);

    /**
     * po => dto
     *
     * @param po source
     * @return target
     */
    NifiCustomWorkflowDetailDTO poToDto(NifiCustomWorkflowDetailPO po);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    NifiCustomWorkflowDetailPO childrenDtoToPo(ChildrenWorkflowDetailDTO dto);


    /**
     * Dto => ChildrenDto
     *
     * @param dto source
     * @return target
     */
    ChildrenWorkflowDetailDTO childrenDtoToDto(NifiCustomWorkflowDetailDTO dto);
    /**
     * list集合 dto -> po
     *
     * @param list list
     * @return target
     */
    List<NifiCustomWorkflowDetailPO> listDtoToPo(List<NifiCustomWorkflowDetailDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list list
     * @return target
     */
    List<NifiCustomWorkflowDetailDTO> listPoToDto(List<NifiCustomWorkflowDetailPO> list);

    /**
     * list集合 dto -> po
     *
     * @param list list
     * @return target
     */
    List<NifiCustomWorkflowDetailPO> listChildrenDtoToPo(List<ChildrenWorkflowDetailDTO> list);


    /**
     * list集合 Dto -> ChildrenDto
     * @param list
     * @return
     */
    List<ChildrenWorkflowDetailDTO> listChildrenDtoToDto(List<NifiCustomWorkflowDetailDTO> list);
}
