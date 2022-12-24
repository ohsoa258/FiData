package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告通知VO
 * @date 2022/3/22 15:37
 */
@Data
public class QualityReportNoticeVO {
    /**
     * 报告通知Id
     */
    @ApiModelProperty(value = "报告通知Id")
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
     * 接收人列表
     */
    @ApiModelProperty(value = "接收人列表")
    public List<QualityReportRecipientVO> recipients;
}
