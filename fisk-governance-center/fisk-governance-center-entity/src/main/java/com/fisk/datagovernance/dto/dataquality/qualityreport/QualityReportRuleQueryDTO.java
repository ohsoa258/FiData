package com.fisk.datagovernance.dto.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告规则规则查询DTO
 * @date 2022/3/24 14:31
 */
@Data
public class QualityReportRuleQueryDTO {
    /**
     * 报告id
     */
    @ApiModelProperty(value = "报告id")
    public int reportId;

    /**
     * 表全名称（含架构名）/表Id
     */
    @ApiModelProperty(value = "表全名称（含架构名）/表Id")
    public String tableUnique;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数")
    public int size;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    public int current;
}
