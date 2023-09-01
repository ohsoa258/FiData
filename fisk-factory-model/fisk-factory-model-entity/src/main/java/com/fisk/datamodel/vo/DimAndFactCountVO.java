package com.fisk.datamodel.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DimAndFactCountVO {

    /**
     * 维度表总数
     */
    @ApiModelProperty(value = "维度表总数")
    private Integer dimCount;

    /**
     * 事实表总数
     */
    @ApiModelProperty(value = "事实表总数")
    private Integer factCount;

}
