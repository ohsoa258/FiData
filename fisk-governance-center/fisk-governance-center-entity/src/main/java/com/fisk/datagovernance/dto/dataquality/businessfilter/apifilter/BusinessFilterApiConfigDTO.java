package com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗配置DTO
 * @date 2022/10/8 16:20
 */
@Data
public class BusinessFilterApiConfigDTO {

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
     * api授权地址
     */
    @ApiModelProperty(value = "api授权地址")
    public String apiAuthAddress;

    /**
     * api授权body类型  form-data/raw
     */
    @ApiModelProperty(value = "api授权body类型  form-data/raw")
    public String apiAuthBodyType;

    /**
     * api授权票据
     */
    @ApiModelProperty(value = "api授权票据")
    public String apiAuthTicket;

    /**
     * api授权有效时间，分钟
     */
    @ApiModelProperty(value = "api授权有效时间，分钟")
    public int apiAuthExpirMinute;

    /**
     * api地址
     */
    @ApiModelProperty(value = "api地址")
    public String apiAddress;

    /**
     * api body类型  form-data/raw
     */
    @ApiModelProperty(value = "api body类型  form-data/raw")
    public String apiBodyType;

    /**
     * api请求类型：Post/Get
     */
    @ApiModelProperty(value = "api请求类型：Post/Get")
    public String apiRequestType;

    /**
     * api参数范围：Headers/Body
     */
    @ApiModelProperty(value = "api参数范围：Headers/Body")
    public String apiParamRange;
}
