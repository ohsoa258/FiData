package com.fisk.mdm.entity;

import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.ObjectTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/4/5 15:08
 * 事件日志
 */
@Data
public class EventLogPO extends BasePO {

    /**
     * 对象id
     */
    private int objectId;
    /**
     * 对象类型（0、模型；1、实体；2、属性）
     */
    private ObjectTypeEnum objectType;
    /**
     * 事件类型
     */
    private int eventType;
    /**
     * 事件描述
     */
    private String desc;
}
