package com.fisk.mdm.dto.complextype;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-06-01 18:23
 */
@Data
public class GeographyDTO {

    /**
     * 唯一编码
     */
    @ApiModelProperty(value = "唯一编码")
    private String code;

    /**
     * 经度
     */
    @ApiModelProperty(value = "经度")
    private String lng;

    /**
     * 维度
     */
    @ApiModelProperty(value = "维度")
    private String lat;

    /**
     * 地图类型：0:高德类型，1:百度类型
     */
    @ApiModelProperty(value = "地图类型：0:高德类型，1:百度类型")
    private Integer mapType;

    /**
     * 版本id
     */
    @ApiModelProperty(value = "版本id")
    private Integer versionId;

}
