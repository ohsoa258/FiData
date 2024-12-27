package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志
 * @date 2022/4/2 11:07
 */
@Data
public class DataCheckLogExcelVO {
    /**
     * 报告名称
     */
    @ApiModelProperty(value = "报告名称")
    public String reportName;
    /**
     * 数据校验规则名称(代号)
     */
    @ApiModelProperty(value = "数据校验规则名称(代号)")
    public String ruleName;

    /**
     * 检查规则说明
     */
    @ApiModelProperty(value = "检查规则说明")
    public String checkRuleIllustrate;

    /**
     * 检查模板名称
     */
    @ApiModelProperty(value = "检查模板名称")
    public String checkTemplateName;

    /**
     * 表架构名称
     */
    @ApiModelProperty(value = "表架构名称")
    public String schemaName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;


    /**
     * 检查结果
     */
    @ApiModelProperty(value = "检查结果")
    public String checkResult;

    /**
     * 检查数据的总条数
     */
    @ApiModelProperty(value = "检查数据的总条数")
    public String checkTotalCount;

    /**
     * 检查数据不通过的条数
     */
    @ApiModelProperty(value = "检查数据不通过的条数")
    public String checkFailCount;

    /**
     * 检查数据开始时间
     */
    @ApiModelProperty(value = "检查数据开始时间")
    public String checkDataStartTime;

    /**
     * 检查数据所需时长，单位：秒
     */
    @ApiModelProperty(value = "检查数据所需时长，单位：秒")
    public String checkDataDuration;

    /**
     * 质量分析
     */
    @ApiModelProperty(value = "质量分析")
    public String qualityAnalysis;
}
