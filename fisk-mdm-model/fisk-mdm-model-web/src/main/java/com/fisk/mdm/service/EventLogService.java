package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;

/**
 * @author WangYan
 * @date 2022/4/6 16:34
 */
public interface EventLogService {

    /**
     * 记录事件日志
     * @param id
     * @param objectType
     * @param eventType
     * @param desc
     * @return
     */
    ResultEnum saveEventLog(Integer id, ObjectTypeEnum objectType, EventTypeEnum eventType, String desc);
}
