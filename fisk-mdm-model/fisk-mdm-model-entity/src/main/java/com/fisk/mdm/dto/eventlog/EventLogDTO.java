package com.fisk.mdm.dto.eventlog;

import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ObjectTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/4/6 16:36
 */
@Data
public class EventLogDTO {

    /**
     * 对象id
     */
    private Integer objectId;
    /**
     * 对象类型（0、模型；1、实体；2、属性）
     */
    private ObjectTypeEnum objectType;
    /**
     * 事件类型
     */
    private EventTypeEnum eventType;
    /**
     * 事件描述
     */
    private String desc;
}
