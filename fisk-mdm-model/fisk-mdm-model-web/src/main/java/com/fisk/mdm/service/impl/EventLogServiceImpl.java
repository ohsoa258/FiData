package com.fisk.mdm.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.eventlog.EventLogDTO;
import com.fisk.mdm.entity.EventLogPO;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import com.fisk.mdm.map.EventLogMap;
import com.fisk.mdm.mapper.EventLogMapper;
import com.fisk.mdm.service.EventLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2022/4/6 16:35
 */
@Service
public class EventLogServiceImpl implements EventLogService {

    @Resource
    EventLogMapper logMapper;

    @Override
    public ResultEnum saveEventLog(Integer id,ObjectTypeEnum objectType,EventTypeEnum eventType,String desc) {

        // 保存事件日志
        EventLogDTO eventLog = new EventLogDTO();
        eventLog.setObjectId(id);
        eventLog.setObjectType(objectType);
        eventLog.setEventType(eventType);
        eventLog.setDesc(desc);
        EventLogPO logPo = EventLogMap.INSTANCES.dtoToPo(eventLog);

        int res = logMapper.insert(logPo);
        return res <= 0 ? ResultEnum.SAVE_DATA_ERROR : ResultEnum.SUCCESS;
    }
}
