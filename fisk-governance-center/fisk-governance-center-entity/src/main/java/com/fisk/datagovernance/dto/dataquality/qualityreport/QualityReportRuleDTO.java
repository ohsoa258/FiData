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
     * 规则id
     */
    @ApiModelProperty(value = "规则id")
    public int ruleId;
}
