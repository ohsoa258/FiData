package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.eventlog.EventLogDTO;

/**
 * @author WangYan
 * @date 2022/4/6 16:34
 */
public interface EventLogService {

    /**
     * 记录事件日志
     * @param dto
     * @return
     */
    ResultEnum saveEventLog(EventLogDTO dto);
}
