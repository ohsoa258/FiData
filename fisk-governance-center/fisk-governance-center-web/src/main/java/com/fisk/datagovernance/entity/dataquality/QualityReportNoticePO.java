package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告通知方式
 * @date 2022/11/29 12:19
 */
@Data
@TableName("tb_quality_report_notice")
public class QualityReportNoticePO extends BasePO {
    /**
     * 报告id
     */
    public int reportId;

    /**
     * 报告通知类型 1、邮件通知 2、站内通知 3、微信通知 4、短信通知
     */
    public int reportNoticeType;

    /**
     * 邮件配置表id
     */
    public int emailServerId;

    /**
     * 邮件主题
     */
    public String subject;

    /**
     * 正文
     */
    public String body;
}
