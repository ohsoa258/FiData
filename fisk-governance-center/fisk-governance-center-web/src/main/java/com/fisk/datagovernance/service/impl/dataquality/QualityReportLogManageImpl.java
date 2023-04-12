package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.QualityReportLogPO;
import com.fisk.datagovernance.mapper.dataquality.QualityReportLogMapper;
import com.fisk.datagovernance.service.dataquality.IQualityReportLogManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告日志
 * @date 2022/11/29 14:02
 */
@Service
public class QualityReportLogManageImpl extends ServiceImpl<QualityReportLogMapper, QualityReportLogPO> implements IQualityReportLogManageService {
}
