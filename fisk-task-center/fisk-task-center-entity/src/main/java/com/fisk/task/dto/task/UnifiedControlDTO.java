package com.fisk.task.dto.task;

import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UnifiedControlDTO extends MQBaseDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * templateModulesType
     */
    @ApiModelProperty(value = "模板模块类型")
    public OlapTableEnum type;
    /**
     * 表达式类型
     */
    @ApiModelProperty(value = "表达式类型")
    public SchedulingStrategyTypeEnum scheduleType;
    /**
     * 表达式
     */
    @ApiModelProperty(value = "表达式")
    public String scheduleExpression;
    /**
     * topic
     * 数据质量-质量报告  BUILD_GOVERNANCE_TEMPLATE_FLOW
     * 数据安全-智能发现报告  BUILD_DATA_SECURITY_INTELLIGENT_DISCOVERY_FLOW
     */
    @ApiModelProperty(value = "主题")
    public String topic;
    /**
     * 类型：数据接入/数据建模/统一调度
     */
    @ApiModelProperty(value = "类型：数据接入/数据建模/统一调度")
    public DataClassifyEnum dataClassifyEnum;
    /**
     * 是否禁用删除true删除/false不删除
     */
    @ApiModelProperty(value = "是否禁用删除true删除/false不删除")
    public boolean deleted;


}
