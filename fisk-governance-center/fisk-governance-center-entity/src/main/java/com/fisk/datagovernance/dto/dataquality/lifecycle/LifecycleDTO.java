package com.fisk.datagovernance.dto.dataquality.lifecycle;

import com.fisk.datagovernance.enums.dataquality.RuleStateEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TableStateTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期DTO
 * @date 2022/3/24 13:59
 */
@Data
public class LifecycleDTO {
    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 数据源表主键id
     */
    @ApiModelProperty(value = "数据源表主键id")
    public int datasourceId;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum sourceTypeEnum;

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
     * 表名称/表Id
     */
    @ApiModelProperty(value = "表名称/表Id")
    public String tableUnique;

    /**
     * 表类型 1：表  2：视图
     */
    @ApiModelProperty(value = "表类型 1：表  2：视图")
    public int tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    @ApiModelProperty(value = "表业务类型 1：事实表、2：维度表、3、指标表  4、宽表")
    public int tableBusinessType;

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
