package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 17:21
 * 原子指标 四级
 */
@Data
public class AtomicIndicatorsDTO {

    @ApiModelProperty(value = "指示器Id")
    public Long indicatorsId;

    @ApiModelProperty(value = "指示器名称")
    public String indicatorsName;

    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public int dimension;
}
