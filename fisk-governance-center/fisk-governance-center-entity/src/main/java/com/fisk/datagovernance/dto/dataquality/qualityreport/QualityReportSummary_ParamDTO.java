package com.fisk.datagovernance.dto.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告总结参数DTO
 * @date 2024/7/17 13:10
 */
@Data
public class QualityReportSummary_ParamDTO {
    /**
     * 表架构名称
     */
    @ApiModelProperty(value = "表架构名称")
    public String schemaName;

    /**
     * 表名称-带架构名
     */
    @ApiModelProperty(value = "表名称-带架构名")
    public String tableName;

    /**
     * 表名称-带转义处理带架构名
     */
    @ApiModelProperty(value = "表名称-带转义处理带架构名")
    public String tableNameFormat;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段名称-带转义处理
     */
    @ApiModelProperty(value = "字段名称-带转义处理")
    public String fieldNameFormat;

    /**
     * 指定查询的字段名称，多个逗号分隔
     */
    @ApiModelProperty(value = "指定查询的字段名称，多个逗号分隔")
    public String allocateFieldNames;

    /**
     * 指定查询的字段名称，多个逗号分隔-带转义处理
     */
    @ApiModelProperty(value = "指定查询的字段名称，多个逗号分隔-带转义处理")
    public String allocateFieldNamesFormat;

    /**
     * 字段检查条件
     */
    @ApiModelProperty(value = "字段检查条件")
    public String fieldCheckWhereSql;

    /**
     * 报告批次号
     */
    @ApiModelProperty(value = "报告批次号")
    public String reportBatchNumber;
}
