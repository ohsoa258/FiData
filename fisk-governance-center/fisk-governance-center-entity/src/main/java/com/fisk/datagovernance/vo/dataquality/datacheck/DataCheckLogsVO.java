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
public class DataCheckLogsVO {
    /**
     * 数据校验规则id
     */
    @ApiModelProperty(value = "数据校验规则id")
    public int ruleId;

    /**
     * 数据校验规则名称(代号)
     */
    @ApiModelProperty(value = "数据校验规则名称(代号)")
    public String ruleName;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 检查模板名称
     */
    @ApiModelProperty(value = "检查模板名称")
    public String checkTemplateName;

    /**
     * 平台数据源表主键id
     */
    @ApiModelProperty(value = "平台数据源表主键id")
    public int fiDatasourceId;

    /**
     * 日志类型：
     * 1 接口同步数据校验日志（同步前）
     * 2 nifi同步数据校验日志（同步中）
     * 3 订阅报告数据校验日志（同步后）
     */
    @ApiModelProperty(value = "日志类型：1 接口同步数据校验日志（同步前）、2 nifi同步数据校验日志（同步中）、3 订阅报告数据校验日志（同步后）")
    public int logType;

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
     * 检查数据的批次号
     */
    @ApiModelProperty(value = "检查数据的批次号")
    public String checkBatchNumber;

    /**
     * 检查数据的小批次号
     */
    @ApiModelProperty(value = "检查数据的小批次号")
    public String checkSmallBatchNumber;

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
     * 检查结果
     */
    @ApiModelProperty(value = "检查结果")
    public String checkResult;

    /**
     * 检查提示消息
     */
    @ApiModelProperty(value = "检查提示消息")
    public String checkMsg;

    /**
     * 检查规则说明
     */
    @ApiModelProperty(value = "检查规则说明")
    public String checkRuleIllustrate;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public String createTime;
}
