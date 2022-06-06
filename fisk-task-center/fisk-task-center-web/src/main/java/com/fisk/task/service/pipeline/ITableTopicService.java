package com.fisk.task.service.pipeline;

import com.fisk.task.dto.task.TableTopicDTO;

import java.util.List;

/**
 * @author cfk
 */
public interface ITableTopicService {

    /**
     * 获取TableTopicDTO集合
     *
     * @param tableTopicDTO
     * @return List<TableTopicDTO>
     */
    List<TableTopicDTO> getTableTopicList(TableTopicDTO tableTopicDTO);

    /**
     * 更新tableTopicDTO
     *
     * @param tableTopicDTO
     * @return void
     */
    void updateTableTopic(TableTopicDTO tableTopicDTO);

    /**
     * 根据ComponentId更新tableTopicDTO
     *
     * @param tableTopicDTO
     * @return void
     */
    void updateTableTopicByComponentId(TableTopicDTO tableTopicDTO);

    /**
     * 根据ComponentId删除tableTopicDTO
     *
     * @param ids
     * @return Integer
     */
    Integer deleteTableTopicByComponentId(List<Integer> ids);

    /**
     * 根据TableId删除tableTopicDTO
     *
     * @param tableTopicDTO
     * @return Integer
     */
    Integer deleteTableTopicByTableId(TableTopicDTO tableTopicDTO);

    /**
     * 根据ComponentId查询tableTopicDTO
     *
     * @param id
     * @return Integer
     */
    TableTopicDTO getTableTopicDTOByComponentId(Integer id,Integer tableId,Integer tableType);
}
