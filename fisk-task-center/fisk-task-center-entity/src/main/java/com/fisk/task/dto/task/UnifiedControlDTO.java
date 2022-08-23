package com.fisk.task.dto.task;

import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

@Data
public class UnifiedControlDTO extends MQBaseDTO {
    /**
     * id
     */
    public int id;
    /**
     * templateModulesType
     */
    public OlapTableEnum type;
    /**
     * 表达式类型
     */
    public SchedulingStrategyTypeEnum scheduleType;
    /**
     * 表达式
     */
    public String scheduleExpression;
    /**
     * topic
     * 数据质量  BUILD_GOVERNANCE_TEMPLATE_FLOW
     */
    public String topic;
    /**
     * 类型：数据接入/数据建模/统一调度
     */
    public DataClassifyEnum dataClassifyEnum;
    /**
     * 是否禁用删除true删除/false不删除
     */
    public boolean deleted;


}
