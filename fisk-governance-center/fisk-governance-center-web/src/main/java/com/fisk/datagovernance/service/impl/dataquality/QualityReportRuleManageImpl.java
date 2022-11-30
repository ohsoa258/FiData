package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.QualityReportRulePO;
import com.fisk.datagovernance.mapper.dataquality.QualityReportRuleMapper;
import com.fisk.datagovernance.service.dataquality.IQualityReportRuleManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告规则
 * @date 2022/3/25 15:42
 */
@Service
public class QualityReportRuleManageImpl extends ServiceImpl<QualityReportRuleMapper, QualityReportRulePO> implements IQualityReportRuleManageService {
    
}
