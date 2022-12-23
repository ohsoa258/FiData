package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 报告扩展信息-邮件服务器
 * @date 2022/12/23 17:00
 */
@Data
public class QualityReportExt_EmailVO {
    /**
     * id
     */
    @ApiModelProperty(value = "标识")
    public Long id;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    public String name;
}
