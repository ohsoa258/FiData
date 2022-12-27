package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告规则
 * @date 2022/3/22 15:37
 */
@Data
public class QualityReportRuleVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 报告id
     */
    @ApiModelProperty(value = "报告id")
    public int reportId;

    /**
     * 报告类型 100、质量校验报告 200、数据清洗报告
     */
    @ApiModelProperty(value = "报告类型 100、质量校验报告 200、数据清洗报告")
    public int reportType;

    /**
     * 规则id
     */
    @ApiModelProperty(value = "规则id")
    public int ruleId;

    /**
     * 规则执行顺序
     */
    @ApiModelProperty(value = "规则执行顺序")
    public int ruleSort;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 规则类型名称
     */
    @ApiModelProperty(value = "规则类型名称")
    public String ruleTypeName;

    /**
     * 规则状态名称
     */
    @ApiModelProperty(value = "规则状态名称")
    public String ruleStateName;

    /**
     * 数据质量数据源表的数据源ID
     */
    @ApiModelProperty(value = "数据质量数据源表的数据源ID")
    public int dataSourceId;

    /**
     * 数据源类型名称
     */
    @ApiModelProperty(value = "数据源类型名称")
    public String sourceTypeName;

    /**
     * IP
     */
    @ApiModelProperty(value = "IP")
    public String ip;

    /**
     * 库名称
     */
    @ApiModelProperty(value = "库名称")
    public String dbName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    public String tableAliasName;

    /**
     * 表类型
     */
    @ApiModelProperty(value = "表类型")
    public String tableTypeName;

    /**
     * 表名称/表Id
     */
    public String tableUnique;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    public int tableBusinessType;
}
