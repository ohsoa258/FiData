package com.fisk.datagovernance.dto.dataquality.lifecycle;

import com.fisk.datagovernance.enums.dataquality.RuleStateEnum;
import com.fisk.datagovernance.enums.dataquality.TableStateTypeEnum;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期DTO
 * @date 2022/3/24 13:59
 */
public class LifecycleDTO {
    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 回收时间
     */
    @ApiModelProperty(value = "回收时间")
    public String recoveryDate;

    /**
     * 已持续次数
     */
    @ApiModelProperty(value = "已持续次数")
    public int continuedNumber;

    /**
     * 是否需要备份，默认否
     */
    @ApiModelProperty(value = "是否需要备份")
    public int isBackup;

    /**
     * 数据血缘断裂回收模板；
     * 上下游血缘关系范围：
     * 1、上游 2、下游 3、上下游
     */
    @ApiModelProperty(value = "上下游血缘关系范围")
    public int consanguinityRange;

    /**
     * 数据无刷新模板；
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 表状态
     */
    @ApiModelProperty(value = "表状态")
    public TableStateTypeEnum tableState;

    /**
     * 生成规则
     */
    @ApiModelProperty(value = "生成规则")
    public String createRule;

    /**
     * 规则状态
     */
    @ApiModelProperty(value = "规则状态")
    public RuleStateEnum ruleState;
}
