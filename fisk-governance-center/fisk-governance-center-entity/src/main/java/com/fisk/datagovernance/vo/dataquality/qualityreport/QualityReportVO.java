package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告VO
 * @date 2022/3/22 15:38
 */
@Data
public class QualityReportVO {
    /**
     * 报告id
     */
    @ApiModelProperty(value = "id")
    public int id;

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
     * 下次执行时间
     */
    @ApiModelProperty(value = "下次执行时间")
    public String nextRunTime;

    /**
     * 通知内容（质量规则）
     */
    @ApiModelProperty(value = "通知内容（质量规则）")
    public List<QualityReportRuleVO> rules;

    /**
     * 通知方式
     */
    @ApiModelProperty(value = "通知方式")
    public QualityReportNoticeVO notice;
}
