package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告接收人
 * @date 2022/3/22 15:20
 */
@Data
@TableName("tb_quality_report_recipient")
public class QualityReportRecipientPO extends BasePO {
    /**
     * 报告id
     */
    public int reportId;

    /**
     * 用户类型：1、FiData系统用户 2、第三方用户
     * 如果选择FiData系统用户，那么接收人名称和邮件收件人自动带出
     */
    public int userType;

    /**
     * FiData用户ID
     */
    public int userId;

    /**
     * 接收人名称
     */
    public String userName;

    /**
     * 接收人
     */
    public String recipient;
}
