package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 17:23
 * 派生指标
 */
@Data
public class DerivedIndicatorsDTO {

    @ApiModelProperty(value = "指示器id")
    public Long indicatorsId;

    @ApiModelProperty(value = "驱动名称")
    public String derivedName;

    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public int dimension;
}
