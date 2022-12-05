package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告
 * @date 2022/3/22 15:19
 */
@Data
@TableName("tb_quality_report")
public class QualityReportPO extends BasePO {
    /**
     * 报告名称
     */
    public String reportName;

    /**
     * 报告类型 100、质量校验报告 200、数据清洗报告
     */
    public int reportType;

    /**
     * 报告类型名称 质量校验报告/数据清洗报告
     */
    public String reportTypeName;

    /**
     * 报告描述
     */
    public String reportDesc;

    /**
     * 报告负责人
     */
    public String reportPrincipal;

    /**
     * 报告通知类型 1、邮件通知 2、站内通知
     */
    public int reportNoticeType;

    /**
     * 报告状态 1、启用 0、禁用
     */
    public int reportState;

    /**
     * 邮件服务id
     */
    public int emailServerId;

    /**
     * 邮件主题
     */
    public String emailSubject;

    /**
     * 邮件收件人
     */
    public String emailConsignee;

    /**
     * 邮件抄送人
     */
    public String emailCc;

    /**
     * 消息正文
     */
    public String body;

    /**
     * 发送频率
     */
    public String runTimeCron;
}
