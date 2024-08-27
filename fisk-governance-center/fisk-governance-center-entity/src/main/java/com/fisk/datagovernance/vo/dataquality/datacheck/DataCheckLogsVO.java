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
     * 日志主键id
     */
    @ApiModelProperty(value = "日志主键id")
    public int id;

    /**
     * uuid,用于关联附件表的object_id
     */
    @ApiModelProperty(value = "uuid,用于关联附件表的object_id")
    public String idUuid;

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
     * 3 订阅报告规则校验日志（同步后）
     */
    @ApiModelProperty(value = "日志类型：1 接口同步数据校验日志（同步前）、2 nifi同步数据校验日志（同步中）、3 订阅报告规则校验日志（同步后）")
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
     * 检查数据的正确率
     */
    @ApiModelProperty(value = "检查数据的正确率")
    public String checkDataAccuracy;

    /**
     * 检查数据开始时间
     */
    @ApiModelProperty(value = "检查数据开始时间")
    public String checkDataStartTime;

    /**
     * 检查数据结束时间
     */
    @ApiModelProperty(value = "检查数据结束时间")
    public String checkDataEndTime;

    /**
     * 检查数据所需时长，单位：秒
     */
    @ApiModelProperty(value = "检查数据所需时长，单位：秒")
    public String checkDataDuration;

    /**
     * 检查错误数据的SQL语句
     */
    @ApiModelProperty(value = "检查错误数据的SQL语句")
    public String checkDataSql;

    /**
     * 检查数据总条数的SQL语句
     */
    @ApiModelProperty(value = "检查数据总条数的SQL语句")
    public String checkDataCountSql;

    /**
     * 检查错误数据条数的SQL语句
     */
    @ApiModelProperty(value = "检查错误数据条数的SQL语句")
    public String checkErrorDataCountSql;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public String createTime;

    /**
     * 报告名称
     */
    @ApiModelProperty(value = "报告名称")
    public String reportName;

    /**
     * 数据库IP
     */
    @ApiModelProperty(value = "数据库IP")
    public String dataBaseIp;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String dataBaseName;

    /**
     * 是否存在报告
     */
    @ApiModelProperty(value = "是否存在规则报告")
    public boolean existReport;

    /**
     * 报告名称
     */
    @ApiModelProperty(value = "规则报告名称")
    public String originalName;

    /**
     * 质量分析
     */
    @ApiModelProperty(value = "质量分析")
    public String qualityAnalysis;
}
