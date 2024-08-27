package com.fisk.datagovernance.dto.dataquality.qualityreport;

import com.fisk.common.core.utils.Dto.Excel.SheetDataDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告规则检查明细
 * @date 2024/7/17 13:10
 */
@Data
public class QualityReportSummary_RuleDTO {
    /**
     * 检查规则名称
     */
    @ApiModelProperty(value = "检查规则名称")
    public String ruleName;

    /**
     * 检查规则描述
     */
    @ApiModelProperty(value = "检查规则描述")
    public String ruleDescribe;

    /**
     * 检查规则说明
     */
    @ApiModelProperty(value = "检查规则说明")
    public String ruleIllustrate;

    /**
     * 检查规则类型（属于那个模板）
     */
    @ApiModelProperty(value = "检查规则类型（属于那个模板）")
    public String ruleTemplate;

    /**
     * 检查的表名称，带架构
     */
    @ApiModelProperty(value = "检查的表名称，带架构")
    public String tableFullName;

    /**
     * 检查的字段名称
     */
    @ApiModelProperty(value = "检查的字段名称")
    public String fieldName;

    /**
     * 检查的数据条数
     */
    @ApiModelProperty(value = "检查的数据条数")
    public Integer checkDataCount;

    /**
     * 检查出来的错误数据条数
     */
    @ApiModelProperty(value = "检查出来的错误数据条数")
    public Integer checkErrorDataCount;

    /**
     * 检查的数据正确率
     */
    @ApiModelProperty(value = "数据的正确率")
    public String dataAccuracy;

    /**
     * 是否检查通过
     */
    @ApiModelProperty(value = "是否检查通过")
    public String checkStatus;

    /**
     * 检查错误数据的SQL语句
     */
    @ApiModelProperty(value = "检查错误数据的SQL语句")
    public String checkDataSql;

    /**
     * 检查数据总条数的SQL语句
     */
    @ApiModelProperty(value = "检查数据总条数的SQL语句")
    public String checkTotalCountSql;

    /**
     * 检查错误数据条数的SQL语句
     */
    @ApiModelProperty(value = "检查错误数据条数的SQL语句")
    public String checkErrorDataCountSql;

    /**
     * 检查出来的错误数据
     */
    @ApiModelProperty(value = "检查出来的错误数据")
    public SheetDataDto sheetData;

    /**
     * 质量分析
     */
    @ApiModelProperty(value = "质量分析")
    public String qualityAnalysis;
}
