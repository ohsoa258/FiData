package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告规则
 * @date 2022/3/22 15:20
 */
@Data
@TableName("tb_quality_report_rule")
public class QualityReportRulePO extends BasePO {
    /**
     * 报告id
     */
    public int reportId;


    /**
     * 规则id
     */
    public int ruleId;
}
