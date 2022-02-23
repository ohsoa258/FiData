package com.fisk.task.service.pipeline.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.enums.task.TopicTypeEnum;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.mapper.TableTopicMapper;
import com.fisk.task.service.pipeline.ITableTopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class TableTopicImpl extends ServiceImpl<TableTopicMapper, TableTopicDTO> implements ITableTopicService {

    @Resource
    TableTopicMapper tableTopicMapper;


    @Override
    public List<TableTopicDTO> getTableTopicList(TableTopicDTO tableTopicDTO) {
        HashMap<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("table_id", tableTopicDTO.tableId);
        conditionMap.put("table_type", tableTopicDTO.tableType);
        conditionMap.put("del_flag", 1);
        if (tableTopicDTO.topicType != 0) {
            conditionMap.put("topic_type", tableTopicDTO.topicType);
        }
        List<TableTopicDTO> tableTopicDTOS = tableTopicMapper.selectByMap(conditionMap);
        return tableTopicDTOS;
    }

    @Override
    public void updateTableTopic(TableTopicDTO tableTopicDTO) {
        List<TableTopicDTO> tableTopicList = this.getTableTopicList(tableTopicDTO);
        if (tableTopicList != null && tableTopicList.size() != 0) {
            tableTopicDTO.id = tableTopicList.get(0).id;
            tableTopicMapper.updateById(tableTopicDTO);
        } else {
            tableTopicMapper.insert(tableTopicDTO);
        }
    }

    @Override
    public void updateTableTopicByComponentId(TableTopicDTO tableTopicDTO) {
        HashMap<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("component_id", tableTopicDTO.componentId);
        conditionMap.put("del_flag", 1);
        if(tableTopicDTO.topicType!=0){
            conditionMap.put("topic_type",tableTopicDTO.topicType);
        }
        List<TableTopicDTO> dtoList = tableTopicMapper.selectByMap(conditionMap);
        if (dtoList != null && dtoList.size() != 0) {
            tableTopicDTO.id = dtoList.get(0).id;
            tableTopicMapper.updateById(tableTopicDTO);
        } else {
            tableTopicMapper.insert(tableTopicDTO);
        }
    }

    @Override
    public Integer deleteTableTopicByComponentId(List<Integer> ids) {
        HashMap<String, Object> conditionMap = new HashMap<>();
        String ComponentId = "";
        for (Integer id : ids) {
            ComponentId += id + " or ";
        }
        conditionMap.put("nifi_custom_workflow_detail_id", ComponentId);
        conditionMap.put("del_flag", 1);
        int i = tableTopicMapper.deleteByMap(conditionMap);
        return i;
    }

    @Override
    public Integer deleteTableTopicByTableId(TableTopicDTO tableTopicDTO) {
        HashMap<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("table_id", tableTopicDTO.tableId);
        conditionMap.put("table_type", tableTopicDTO.tableType);
        conditionMap.put("topic_type", TopicTypeEnum.DAILY_NIFI_FLOW.getValue());
        conditionMap.put("del_flag", 1);
        int i = tableTopicMapper.deleteByMap(conditionMap);
        return i;
    }

    @Override
    public TableTopicDTO getTableTopicDTOByComponentId(Integer id) {
        TableTopicDTO topicDTO = new TableTopicDTO();
        HashMap<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("component_id", id);
        conditionMap.put("del_flag", 1);
        conditionMap.put("topic_type", TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue());
        List<TableTopicDTO> tableTopicDTOS = tableTopicMapper.selectByMap(conditionMap);
        if (tableTopicDTOS != null && tableTopicDTOS.size() != 0) {
            return tableTopicDTOS.get(0);
        }
        return topicDTO;
    }


}
