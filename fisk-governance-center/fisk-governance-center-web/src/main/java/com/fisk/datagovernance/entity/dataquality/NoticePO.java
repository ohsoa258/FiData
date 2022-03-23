package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知表
 * @date 2022/3/22 15:19
 */
@Data
@TableName("tb_notice_module")
public class NoticePO extends BasePO {
    /**
     * 组件名称
     */
    public String moduleName;

    /**
     * 通知类型
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
     * 组件状态
     */
    public int moduleState;
}
