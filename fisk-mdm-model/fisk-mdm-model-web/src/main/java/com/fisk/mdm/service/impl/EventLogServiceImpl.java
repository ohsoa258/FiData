package com.fisk.mdm.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.eventlog.EventLogDTO;
import com.fisk.mdm.entity.EventLogPO;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.EventLogMap;
import com.fisk.mdm.mapper.EventLogMapper;
import com.fisk.mdm.service.EventLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2022/4/6 16:35
 */
@Slf4j
@Service
public class EventLogServiceImpl implements EventLogService {

    @Resource
    EventLogMapper logMapper;

    @Override
    public ResultEnum saveEventLog(Integer id,ObjectTypeEnum objectType,EventTypeEnum eventType,String desc) {

        // 保存事件日志
        Integer res = null;
        try{
            EventLogDTO eventLog = new EventLogDTO();
            eventLog.setObjectId(id);
            eventLog.setObjectType(objectType);
            eventLog.setEventType(eventType);
            eventLog.setDesc(desc);
            EventLogPO logPo = EventLogMap.INSTANCES.dtoToPo(eventLog);

            res = logMapper.insert(logPo);
        }catch (Exception ex){
            log.info("保存事件日志报错,错误信息:" + ex.getMessage());
        }
        return res <= 0 ? ResultEnum.SAVE_DATA_ERROR : ResultEnum.SUCCESS;
    }
}
