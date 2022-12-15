package com.fisk.task.service.pipeline;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.task.dto.task.TableTopicDTO;

import java.util.List;

/**
 * @author cfk
 */
public interface ITableTopicService extends IService<TableTopicDTO> {

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
    TableTopicDTO getTableTopicDTOByComponentId(Integer id, String tableId, Integer tableType);

    /**
     * 根据管道topic找到实际topic
     *
     * @param topicName
     * @return List<TableTopicDTO>
     */
    List<TableTopicDTO> getByTopicName(String topicName);

    /**
     * 管道删除组件,同时删除topic
     *
     * @param dtos
     * @return
     */
    boolean deleteTableTopicGroup(List<TableTopicDTO> dtos);
}
