package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验DTO
 * @date 2022/3/24 13:27
 */
@Data
public class DataCheckDTO {
    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源表主键id")
    public int datasourceId;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum sourceType;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 架构名称
     */
    @ApiModelProperty(value = "架构名称")
    public String schemaName;

    /**
     * 表名称/表Id
     */
    @ApiModelProperty(value = "表名称/表Id")
    public String tableUnique;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 表类型 1：表  2：视图
     */
    @ApiModelProperty(value = "表类型 TABLE/VIEW")
    public TableTypeEnum tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    @ApiModelProperty(value = "表业务类型 1：事实表、2：维度表、3、指标表  4、宽表")
    public TableBusinessTypeEnum tableBusinessType;

    /**
     * 规则检查类型：1、强规则 2、弱规则
     */
    @ApiModelProperty(value = "规则检查类型：1、强规则 2、弱规则")
    public RuleCheckTypeEnum ruleCheckType;

    /**
     * 规则执行节点：1、同步前 2、同步中 3、同步后
     */
    @ApiModelProperty(value = "规则执行节点：1、同步前 2、同步中 3、同步后")
    public RuleExecuteNodeTypeEnum ruleExecuteNode;

    /**
     * 规则执行顺序
     */
    @ApiModelProperty(value = "规则执行顺序")
    public int ruleExecuteSort;

    /**
     * 规则权重
     */
    @ApiModelProperty(value = "规则权重")
    public int ruleWeight;

    /**
     * 规则描述
     */
    @ApiModelProperty(value = "规则描述")
    public String ruleDescribe;

    /**
     * 规则状态：1、启用 0、禁用
     */
    @ApiModelProperty(value = "规则状态：1、启用 0、禁用")
    public RuleStateEnum ruleState;

    /**
     * 规则说明
     */
    @ApiModelProperty(value = "规则说明")
    public String ruleIllustrate;

    /**
     * 数据校验规则扩展属性
     */
    @ApiModelProperty(value = "数据校验规则扩展属性")
    public DataCheckExtendDTO dataCheckExtend;
}
