package com.fisk.task.service.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.map.TableTopicMap;
import com.fisk.task.mapper.TableTopicMapper;
import com.fisk.task.po.TableTopicPO;
import com.fisk.task.service.pipeline.ITableTopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author cfk
 */
@Service
@Slf4j
public class TableTopicImpl extends ServiceImpl<TableTopicMapper, TableTopicPO> implements ITableTopicService {

    @Resource
    TableTopicMapper tableTopicMapper;
    @Resource
    RedisUtil redisUtil;


    @Override
    public List<TableTopicDTO> getTableTopicList(TableTopicDTO tableTopicDTO) {
        HashMap<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("table_id", tableTopicDTO.tableId);
        conditionMap.put("table_type", tableTopicDTO.tableType);
        conditionMap.put("del_flag", 1);
        if (tableTopicDTO.topicType != 0) {
            conditionMap.put("topic_type", tableTopicDTO.topicType);
        }
        List<TableTopicDTO> tableTopicDtos = TableTopicMap.INSTANCES.listPoToDto(tableTopicMapper.selectByMap(conditionMap));
        return tableTopicDtos;
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
        if (tableTopicDTO.topicType != 0) {
            conditionMap.put("topic_type", tableTopicDTO.topicType);
        }
        conditionMap.put("table_id", tableTopicDTO.tableId);
        conditionMap.put("table_type", tableTopicDTO.tableType);
        conditionMap.put("topic_type", tableTopicDTO.topicType);
        conditionMap.put("workflow_id",tableTopicDTO.workflowId);

        List<TableTopicDTO> dtoList = TableTopicMap.INSTANCES.listPoToDto(tableTopicMapper.selectByMap(conditionMap));
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
        Integer i = 0;
        for (Integer id : ids) {
            conditionMap.put("component_id", id);
            conditionMap.put("del_flag", 1);
            i = tableTopicMapper.deleteByMap(conditionMap);
        }
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
    public TableTopicDTO getTableTopicDTOByComponentId(Integer id, String tableId, Integer tableType) {
        TableTopicDTO topicDTO = new TableTopicDTO();
        HashMap<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("component_id", id);
        conditionMap.put("del_flag", 1);
        if (!StringUtils.isEmpty(tableId)) {
            conditionMap.put("table_id", Integer.valueOf(tableId));
        }
        conditionMap.put("table_type", tableType);
        conditionMap.put("topic_type", TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue());
        List<TableTopicDTO> tableTopicDtos = TableTopicMap.INSTANCES.listPoToDto(tableTopicMapper.selectByMap(conditionMap));
        if (tableTopicDtos != null && tableTopicDtos.size() != 0) {
            return tableTopicDtos.get(0);
        }
        return topicDTO;
    }

    @Override
    public List<TableTopicDTO> getTableTopicDTOByComponentId(List<Long> componentId) {
        LambdaQueryWrapper<TableTopicPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableTopicPO::getDelFlag,1).in(TableTopicPO::getComponentId,componentId);
        List<TableTopicPO> tableTopicPOS = tableTopicMapper.selectList(queryWrapper);
        List<TableTopicDTO> tableTopicDtos = TableTopicMap.INSTANCES.listPoToDto(tableTopicPOS);
        return tableTopicDtos;
    }

    @Override
    public List<TableTopicDTO> getByTopicName(String topicName,String pipelTraceId) {
        List<TableTopicPO> tableTopics = new ArrayList<>();

        List<TableTopicPO> list = this.query().eq("topic_name", topicName).eq("del_flag", 1).list();
        for (TableTopicPO dto : list) {
            List<TableTopicPO> tableTopicPOList = new ArrayList<>();
            int tableId = dto.tableId;
            int tableType = dto.tableType;
            int topicType = dto.topicType;
            if (topicType == TopicTypeEnum.PIPELINE_NIFI_FLOW.getValue()){
                topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
            }
            int taskId = dto.componentId;
            //无表的组件走if里面
            if (Objects.equals(tableId, 0)) {
                tableTopicPOList = this.query().eq("table_type", tableType).eq("component_id", taskId)
                        .eq("topic_type", topicType).eq("del_flag", 1).like("topic_name", topicName).list();
            } else {
                TableTopicPO tableTopic = this.query().eq("table_id", tableId).eq("table_type", tableType)
                        .eq("topic_type", topicType).eq("del_flag", 1).like("topic_name", topicName).one();
                //查找当前组件内所有任务的topic
                List<Long> componentIds = new ArrayList<>();
                Map<Object, Object> hmTask = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
                TaskHierarchyDTO taskHierarchyDTO = JSON.parseObject(hmTask.get(tableTopic.componentId.toString()).toString(), TaskHierarchyDTO.class);
                Iterator<Map.Entry<Object, Object>> nodeMap = hmTask.entrySet().iterator();
                while (nodeMap.hasNext()) {
                    Map.Entry<Object, Object> next = nodeMap.next();
                    TaskHierarchyDTO taskNodeHierarchyDTO = JSON.parseObject(next.getValue().toString(), TaskHierarchyDTO.class);
                    //查找组件内的成员
                    if (taskNodeHierarchyDTO.itselfPort.pid.intValue() == taskHierarchyDTO.itselfPort.pid){
                        componentIds.add(taskNodeHierarchyDTO.itselfPort.id);
                    }
                }
                 tableTopicPOList = this.query().eq("del_flag", 1).in("component_id", componentIds).list();
            }
            tableTopics.addAll(tableTopicPOList);
        }
        return TableTopicMap.INSTANCES.listPoToDto(tableTopics);
    }

    @Override
    public boolean deleteTableTopicGroup(List<TableTopicDTO> dtos) {
        boolean ifsuccess = true;
        for (TableTopicDTO topic : dtos) {
            QueryWrapper<TableTopicPO> TableTopicWrapper = new QueryWrapper<>();
            TableTopicWrapper.lambda().eq(TableTopicPO::getTableId, topic.tableId).eq(TableTopicPO::getTableType, topic.tableType)
                    .eq(TableTopicPO::getTopicType, topic.topicType).like(TableTopicPO::getTopicName, topic.topicName);
            boolean remove = this.remove(TableTopicWrapper);
            if (!remove) {
                ifsuccess = false;
            }
        }
        return ifsuccess;
    }


}
