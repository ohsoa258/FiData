package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告扩展信息VO
 * @date 2022/12/1 10:46
 */
@Data
public class QualityReportExtMapVO {

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
