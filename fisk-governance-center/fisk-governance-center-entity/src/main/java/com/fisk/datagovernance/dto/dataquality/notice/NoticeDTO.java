package com.fisk.datagovernance.dto.dataquality.notice;

import com.fisk.datagovernance.enums.dataquality.ModuleStateEnum;
import com.fisk.datagovernance.enums.dataquality.NoticeTypeEnum;
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
     * 组件名称
     */
    @ApiModelProperty(value = "组件名称")
    public String moduleName;

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
     * 组件状态
     */
    @ApiModelProperty(value = "组件状态")
    public ModuleStateEnum moduleState;

    /**
     * 组件通知关联DTO
     */
    @ApiModelProperty(value = "组件通知关联DTO")
    public List<ComponentNotificationDTO> componentNotificationDTOS;
}
