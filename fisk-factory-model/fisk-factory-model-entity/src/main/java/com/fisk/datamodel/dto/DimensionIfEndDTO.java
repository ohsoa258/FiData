package com.fisk.datamodel.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author LiShiJi
 */
@Data
public class DimensionIfEndDTO {

    @ApiModelProperty(value = "结果")
    public String result;

    @ApiModelProperty(value = "如果继续")
    public int ifGoOn;

    @ApiModelProperty(value = "准时")
    public String goOnTime;

}
