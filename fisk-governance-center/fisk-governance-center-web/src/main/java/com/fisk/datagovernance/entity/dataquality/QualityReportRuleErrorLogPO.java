package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告规则错误日志
 * @date 2022/3/22 15:20
 */
@Data
@TableName("tb_quality_report_rule_error_log")
public class QualityReportRuleErrorLogPO {
    /**
     * 批次号
     */
    public String batchNo;

    /**
     * 报告名称
     */
    public String reportName;

    /**
     * 规则id
     */
    public int ruleId;

    /**
     * 规则名称
     */
    public String ruleName;

    /**
     * 平台数据源表主键id
     */
    public Integer dataCheckGroupId;

    /**
     * 检查错误数据的SQL语句
     */
    public String checkDataSql;

    /**
     * 检查数据总条数的SQL语句
     */
    public String checkDataCountSql;

    /**
     * 检查错误数据条数的SQL语句
     */
    public String checkErrorDataCountSql;

    /**
     * 错误信息
     */
    public String errorInfo;
}
