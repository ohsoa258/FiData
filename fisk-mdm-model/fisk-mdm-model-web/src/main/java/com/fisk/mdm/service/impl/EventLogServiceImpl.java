package com.fisk.mdm.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.eventlog.EventLogDTO;
import com.fisk.mdm.entity.EventLogPO;
import com.fisk.mdm.map.EntityMap;
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
    public ResultEnum saveEventLog(EventLogDTO dto) {
        EventLogPO logPo = EventLogMap.INSTANCES.dtoToPo(dto);
        int res = logMapper.insert(logPo);
        return res <= 0 ? ResultEnum.SAVE_DATA_ERROR : ResultEnum.SUCCESS;
    }
}
