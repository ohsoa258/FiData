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

    /**
     * 类型名称
     */
    @ApiModelProperty(value = "类型名称")
    public String typeName;

    /**
     * 状态名称
     */
    @ApiModelProperty(value = "状态名称")
    public String stateName;

    /**
     * 顺序
     */
    @ApiModelProperty(value = "顺序")
    public int Sort;
}
