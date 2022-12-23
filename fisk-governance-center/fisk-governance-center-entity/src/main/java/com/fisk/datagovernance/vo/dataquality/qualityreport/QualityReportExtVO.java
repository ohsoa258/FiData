package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告扩展信息VO
 * @date 2022/3/22 15:38
 */
@Data
public class QualityReportExtVO {
    /**
     * 质量校验规则
     */
    @ApiModelProperty(value = "质量校验规则")
    public List<QualityReportExt_RuleVO> rules_c;

    /**
     * 数据清洗规则
     */
    @ApiModelProperty(value = "数据清洗规则")
    public List<QualityReportExt_RuleVO> rules_b;

    /**
     * 邮件列表
     */
    @ApiModelProperty(value = "邮件列表")
    public List<QualityReportExt_EmailVO> emails;

    /**
     * 用户列表
     */
    @ApiModelProperty(value = "用户列表")
    public List<QualityReportExt_UserVO> users;
}
