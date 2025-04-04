package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告日志VO
 * @date 2022/11/29 13:54
 */
@Data
public class QualityReportLogVO {
    /**
     * 报告日志id
     */
    @ApiModelProperty(value = "报告日志id")
    public String id;

    /**
     * 报告id
     */
    @ApiModelProperty(value = "报告id")
    public String reportId;

    /**
     * 报告名称
     */
    @ApiModelProperty(value = "报告名称")
    public String reportName;

    /**
     * 报告类型 100、质量校验报告 200、数据清洗报告
     */
    @ApiModelProperty(value = "报告类型 100、质量校验报告 200、数据清洗报告")
    public int reportType;

    /**
     * 报告类型名称
     */
    @ApiModelProperty(value = "报告类型名称")
    public String reportTypeName;

    /**
     * 报告描述
     */
    @ApiModelProperty(value = "报告描述")
    public String reportDesc;

    /**
     * 报告负责人
     */
    @ApiModelProperty(value = "报告负责人")
    public String reportPrincipal;

    /**
     * 报告通知类型 1、邮件通知 2、站内通知 3、微信通知 4、短信通知
     */
    @ApiModelProperty(value = "报告通知类型 1、邮件通知 2、站内通知 3、微信通知 4、短信通知")
    public int reportNoticeType;

    /**
     * 是否存在报告
     */
    @ApiModelProperty(value = "是否存在报告")
    public boolean existReport;

    /**
     * 报告名称
     */
    @ApiModelProperty(value = "报告名称")
    public String originalName;

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
     * 接收人，多个接收人逗号分割
     */
    @ApiModelProperty(value = "接收人，多个接收人逗号分割")
    public String recipient;

    /**
     * 正文
     */
    @ApiModelProperty(value = "正文")
    public String body;

    /**
     * 发送时间
     */
    @ApiModelProperty(value = "发送时间")
    public String sendTime;

    /**
     * 发送结果
     */
    @ApiModelProperty(value = "发送结果")
    public String sendResult;

    /**
     * 报告质量评级：优良中差
     */
    @ApiModelProperty(value = "报告质量评级：优良中差")
    public String reportQualityGrade;

    /**
     * 报告批次号
     */
    @ApiModelProperty(value = "报告批次号")
    public String reportBatchNumber;

    /**
     * 报告下检查的规则数
     */
    @ApiModelProperty(value = "报告下检查的规则数")
    public String reportRuleCheckCount;

    /**
     * 报告下检查不通过的规则数
     */
    @ApiModelProperty(value = "报告下检查不通过的规则数")
    public String reportRuleCheckErrorCount;

    /**
     * 报告下检查规则的正确率
     */
    @ApiModelProperty(value = "报告下检查规则的正确率")
    public String reportRuleCheckAccuracy;

    /**
     * 报告规则检查结果
     */
    @ApiModelProperty(value = "报告规则检查结果")
    public String reportRuleCheckResult;

    /**
     * 报告规则检查结语
     */
    @ApiModelProperty(value = "报告规则检查结语")
    public String reportRuleCheckEpilogue;

    /**
     * 创建报告开始时间
     */
    @ApiModelProperty(value = "创建报告开始时间")
    public String createReportStartTime;

    /**
     * 创建报告结束时间
     */
    @ApiModelProperty(value = "创建报告结束时间")
    public String createReportEndTime;

    /**
     * 创建报告所需时长，单位：秒
     */
    @ApiModelProperty(value = "创建报告所需时长，单位：秒")
    public String createReportDuration;
}
