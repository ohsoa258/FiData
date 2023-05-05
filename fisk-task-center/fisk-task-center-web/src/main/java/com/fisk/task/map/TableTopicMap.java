package com.fisk.task.map;

import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.po.app.TableTopicPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author cfk
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TableTopicMap {
    TableTopicMap INSTANCES = Mappers.getMapper(TableTopicMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    TableTopicPO dtoToPo(TableTopicDTO dto);

    /**
     * po => dto
     *
     * @param po source
     * @return target
     */
    TableTopicDTO poToDto(TableTopicPO po);

    /**
     * list集合 dto -> po
     *
     * @param list list
     * @return target
     */
    List<TableTopicPO> listDtoToPo(List<TableTopicDTO> list);

    /**
     * list集合 po -> dto
     *
     * @param list list
     * @return target
     */
    List<TableTopicDTO> listPoToDto(List<TableTopicPO> list);

}
