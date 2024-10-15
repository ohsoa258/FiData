package com.fisk.datagovernance.dto.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告通知DTO
 * @date 2022/11/29 17:20
 */
@Data
public class QualityReportNoticeDTO {
    /**
     * 质量报告通知id
     */
    @ApiModelProperty(value = "质量报告通知id")
    public int id;

    /**
     * 报告id
     */
    @ApiModelProperty(value = "报告id")
    public int reportId;

    /**
     * 报告通知类型 1、邮件通知 2、站内通知 3、微信通知 4、短信通知
     */
    @ApiModelProperty(value = "报告通知类型 1、邮件通知 2、站内通知 3、微信通知 4、短信通知")
    public int reportNoticeType;

    /**
     * 邮件服务id
     */
    @ApiModelProperty(value = "邮件服务id")
    public int emailServerId;

    /**
     * 主题
     */
    @ApiModelProperty(value = "主题")
    public String subject;

    /**
     * 正文
     */
    @ApiModelProperty(value = "正文")
    public String body;

    /**
     * 发送方式
     * 0校验完成发送
     * 1校验通过发送
     * 2校验失败发送
     */
    @ApiModelProperty(value = "发送方式")
    public int sendType;

    /**
     * 预警级别
     * 0红色预警
     * 1橙色预警
     * 2黄色预警
     * 3绿色预警
     */
    @ApiModelProperty(value = "预警级别")
    public int warnLevel;

    /**
     * 质量报告接收人
     */
    @ApiModelProperty(value = "质量报告接收人")
    public List<QualityReportRecipientDTO> qualityReportRecipient;
}
