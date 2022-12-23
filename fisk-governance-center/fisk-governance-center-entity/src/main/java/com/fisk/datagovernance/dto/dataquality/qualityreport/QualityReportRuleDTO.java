package com.fisk.datagovernance.dto.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告规则DTO
 * @date 2022/3/24 14:31
 */
@Data
public class QualityReportRuleDTO {
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
}
