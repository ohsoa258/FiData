package com.fisk.common.core.utils.Dto.Excel.dataquality;

import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告总结
 * @date 2024/7/17 13:09
 */
@Data
public class QualityReportSummaryDTO {
    /**
     * 报告名称
     */
    public String reportName;

    /**
     * 报告描述
     */
    public String reportDesc;

    /**
     * 报告负责人
     */
    public String reportPrincipal;

    /**
     * 报告批次号
     */
    public String reportBatchNumber;

    /**
     * 检查的表名称，带架构
     */
    public String tableFullName;

    /**
     * 结语
     */
    public String epilogue;

    /**
     * 报告正文
     */
    public List<QualityReportSummary_BodyDTO> qualityReportSummary_body;
}
