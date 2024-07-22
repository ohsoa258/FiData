package com.fisk.common.core.utils.Dto.Excel.dataquality;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告正文
 * @date 2024/7/17 13:10
 */
@Data
public class QualityReportSummary_BodyDTO {
    /**
     * 检查的表名称，带架构
     */
    public String tableFullName;

    /**
     * 检查规则名称
     */
    public String ruleName;

    /**
     * 检查的数据条数
     */
    public Integer checkDataCount;

    /**
     * 检查的数据正确率
     */
    public String dataAccuracy;

    /**
     * 是否检查通过
     */
    public String checkStatus;
}
