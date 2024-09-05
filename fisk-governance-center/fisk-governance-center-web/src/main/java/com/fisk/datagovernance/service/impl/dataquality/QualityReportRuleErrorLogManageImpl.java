package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.QualityReportRuleErrorLogPO;
import com.fisk.datagovernance.mapper.dataquality.QualityReportRuleErrorLogMapper;
import com.fisk.datagovernance.service.dataquality.IQualityReportRuleErrorLogManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告下规则执行错误日志
 * @date 2022/3/25 15:42
 */
@Service
public class QualityReportRuleErrorLogManageImpl extends ServiceImpl<QualityReportRuleErrorLogMapper, QualityReportRuleErrorLogPO> implements IQualityReportRuleErrorLogManageService {
}
