package com.fisk.datagovernance.dto.dataquality.datacheck;

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
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 实际表名称/表Id
     */
    @ApiModelProperty(value = "表名称/表Id")
    public String tableUnique;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    public String tableAlias;

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
     * 校验规则
     */
    @ApiModelProperty(value = "校验规则")
    public CheckRuleEnum checkRule;

    /**
     * 生成规则
     */
    @ApiModelProperty(value = "生成规则")
    public String createRule;

    /**
     * 规则执行顺序
     */
    @ApiModelProperty(value = "规则执行顺序")
    public int ruleSort;

    /**
     * 规则状态
     */
    @ApiModelProperty(value = "规则状态")
    public RuleStateEnum ruleState;

    /**
     * 波动阈值
     */
    @ApiModelProperty(value = "波动阈值")
    public int thresholdValue;

    /**
     * 数据校验规则扩展属性
     */
    @ApiModelProperty(value = "数据校验规则扩展属性")
    public List<DataCheckExtendDTO> dataCheckExtends;
}
