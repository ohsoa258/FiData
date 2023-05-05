package com.fisk.mdm.dto.complextype;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author WangYan
 * @Date 2022/7/14 16:05
 * @Version 1.0
 */
@Data
public class GeographyDataDTO {

    /**
     * 唯一编码
     */
    @ApiModelProperty(value = "唯一编码")
    private String code;

    /**
     * 经度
     */
    @ApiModelProperty(value = "经度")
    private BigDecimal lng;

    /**
     * 维度
     */
    @ApiModelProperty(value = "维度")
    private BigDecimal lat;

    /**
     * 地图类型：0:高德类型，1:百度类型
     */
    @ApiModelProperty(value = "地图类型：0:高德类型，1:百度类型")
    private Integer map_type;

    /**
     * 版本id
     */
    @ApiModelProperty(value = "版本id")
    private Integer fidata_version_id;
}
