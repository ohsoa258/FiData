package com.fisk.datagovernance.dto.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告DTO
 * @date 2022/3/24 14:30
 */
@Data
public class QualityReportDTO {

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
     * 报告类型名称 质量校验报告/数据清洗报告
     */
    @ApiModelProperty(value = "报告类型名称 质量校验报告/数据清洗报告")
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
     * 报告状态 1、启用 0、禁用
     */
    @ApiModelProperty(value = "报告状态 1、启用 0、禁用")
    public int reportState;

    /**
     * 发送频率
     */
    @ApiModelProperty(value = "发送频率")
    public String runTimeCron;

    /**
     * 质量报告规则
     */
    @ApiModelProperty(value = "质量报告规则")
    public List<QualityReportRuleDTO> qualityReportRule;

    /**
     * 质量报告通知
     */
    @ApiModelProperty(value = "质量报告通知")
    public QualityReportNoticeDTO qualityReportNotice;

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
     * 附件实际名称
     */
    @ApiModelProperty(value = "附件实际名称")
    public String attachmentActualName;

    /**
     * 附件地址
     */
    @ApiModelProperty(value = "附件地址")
    public String attachmentPath;

    /**
     * 公司logo地址
     */
    @ApiModelProperty(value = "公司logo地址")
    public String companyLogoPath;
}
