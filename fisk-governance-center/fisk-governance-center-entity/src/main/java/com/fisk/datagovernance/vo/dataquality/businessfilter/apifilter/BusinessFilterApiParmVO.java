package com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗参数配置VO
 * @date 2022/10/8 16:36
 */
@Data
public class BusinessFilterApiParmVO {
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    public int id;

    /**
     * tb_bizfilter_rule表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_rule表主键ID")
    public int ruleId;

    /**
     * tb_bizfilter_api_config表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_api_config表主键ID")
    public int apiId;

    /**
     * api 参数类型 1：授权请求参数  2：正文请求参数
     */
    @ApiModelProperty(value = "api 参数类型 1：授权请求参数  2：正文请求参数")
    public int apiParmType;

    /**
     * 参数key
     */
    @ApiModelProperty(value = "参数key")
    public String apiParmKey;

    /**
     * 参数value
     */
    @ApiModelProperty(value = "参数value")
    public String apiParmValue;

    /**
     * 参数value标识
     */
    @ApiModelProperty(value = "参数value标识")
    public String apiParmValueUnique;
}
