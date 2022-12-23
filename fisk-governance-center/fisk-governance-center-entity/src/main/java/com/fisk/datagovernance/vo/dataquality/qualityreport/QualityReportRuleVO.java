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
}
