package com.fisk.datagovernance.vo.dataquality.notice;

import com.fisk.datagovernance.enums.dataquality.ModuleStateEnum;
import com.fisk.datagovernance.enums.dataquality.NoticeTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 警告通知VO
 * @date 2022/3/22 15:38
 */
@Data
public class NoticeVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

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
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}
