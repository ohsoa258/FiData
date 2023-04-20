package com.fisk.chartvisual.dto.chartvisual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class IsDimensionDTO {

    @ApiModelProperty(value = "字段ID一")
    public Integer fieldIdOne;
    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "维度一")
    public int dimensionOne;

    @ApiModelProperty(value = "字段ID二")
    public Integer fieldIdTwo;
    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "维度二")
    public int dimensionTwo;
}
