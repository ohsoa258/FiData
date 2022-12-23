package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.QualityReportRecipientPO;
import com.fisk.datagovernance.mapper.dataquality.QualityReportRecipientMapper;
import com.fisk.datagovernance.service.dataquality.IQualityReportRecipientManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告接收人
 * @date 2022/3/25 15:42
 */
@Service
public class QualityReportRecipientManageImpl extends ServiceImpl<QualityReportRecipientMapper, QualityReportRecipientPO> implements IQualityReportRecipientManageService {
}
