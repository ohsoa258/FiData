package com.fisk.datagovernance.dto.dataquality.notice;

import com.fisk.datagovernance.enums.dataquality.NoticeTypeEnum;
import com.fisk.datagovernance.enums.dataquality.RuleStateEnum;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知DTO
 * @date 2022/3/24 14:30
 */
public class NoticeDTO {

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 通知名称
     */
    @ApiModelProperty(value = "通知名称")
    public String noticeName;

    /**
     * 通知类型
     */
    @ApiModelProperty(value = "通知类型")
    public NoticeTypeEnum noticeType;

    /**
     * 邮件配置表id
     */
    @ApiModelProperty(value = "邮件配置表id")
    public int emailServerId;

    /**
     * 邮件主题
     */
    @ApiModelProperty(value = "邮件主题")
    public String emailSubject;

    /**
     * 邮件收件人
     */
    @ApiModelProperty(value = "邮件收件人")
    public String emailConsignee;

    /**
     * 邮件抄送人
     */
    @ApiModelProperty(value = "邮件抄送人")
    public String emailCc;

    /**
     * 通知正文
     */
    @ApiModelProperty(value = "通知正文")
    public String body;

    /**
     * 运行时间cron表达式
     */
    @ApiModelProperty(value = "运行时间cron表达式")
    public String runTimeCron;

    /**
     * 通知状态：1、启用 0、禁用
     */
    @ApiModelProperty(value = "通知状态")
    public RuleStateEnum noticeState;

    /**
     * 通知扩展信息
     */
    @ApiModelProperty(value = "通知扩展信息")
    public List<NoticeExtendDTO> noticeExtends;

    /**
     * 是否发送附件
     */
    @ApiModelProperty(value = "是否发送附件")
    public boolean sendAttachment;

    /**
     * 附件名称
     */
    @ApiModelProperty(value = "附件名称")
    public String attachmentName;

    /**
     * 附件地址
     */
    @ApiModelProperty(value = "附件地址")
    public String attachmentPath;
}
