package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.QualityReportNoticePO;
import com.fisk.datagovernance.mapper.dataquality.QualityReportNoticeMapper;
import com.fisk.datagovernance.service.dataquality.IQualityReportNoticeManageService;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告通知方式
 * @date 2022/3/25 15:42
 */
@Service
public class QualityReportNoticeManageImpl extends ServiceImpl<QualityReportNoticeMapper, QualityReportNoticePO> implements IQualityReportNoticeManageService {

}
