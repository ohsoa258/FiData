package com.fisk.datagovernance.dto.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告接收人
 * @date 2022/3/24 14:30
 */
@Data
public class QualityReportRecipientDTO {
    /**
     * 接收人Id
     */
    @ApiModelProperty(value = "接收人Id")
    public int id;

    /**
     * 报告id
     */
    @ApiModelProperty(value = "报告id")
    public int reportId;

    /**
     * 用户类型：1、FiData系统用户 2、第三方用户
     * 如果选择FiData系统用户，那么接收人名称和邮件收件人自动带出
     */
    @ApiModelProperty(value = "用户类型：1、FiData系统用户 2、第三方用户。如果选择FiData系统用户，那么接收人名称和邮件收件人自动带出")
    public int userType;

    /**
     * FiData用户ID
     */
    @ApiModelProperty(value = "FiData用户ID")
    public int userId;

    /**
     * 接收人名称
     */
    @ApiModelProperty(value = "接收人名称")
    public String userName;

    /**
     * 接收人
     */
    @ApiModelProperty(value = "接收人")
    public String recipient;
}
