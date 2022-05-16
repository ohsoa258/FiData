package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 通知表
 * @date 2022/3/22 15:19
 */
@Data
@TableName("tb_notice_rule")
public class NoticePO extends BasePO {
    /**
     * 模板id
     */
    public int templateId;

    /**
     * 通知名称
     */
    public String noticeName;

    /**
     * 通知类型 1、邮件通知 2、站内通知
     */
    public int noticeType;

    /**
     * 邮件配置表id
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
     * 通知正文
     */
    public String body;

    /**
     * 运行时间cron表达式
     */
    public String runTimeCron;

    /**
     * 通知状态
     */
    public int noticeState;
}
