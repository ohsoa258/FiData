package com.fisk.dataservice.dto.api.doc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description API基础信息
 * @date 2022/2/3 14:09
 */
@Data
public class ApiBasicInfoDTO {
    /**
     * api名称
     */
    @ApiModelProperty(value = "参数aip名称")
    public String apiName;

    /**
     * api名称 目录
     */
    @ApiModelProperty(value = "api名称 目录")
    public String apiNameCatalogue;

    /**
     * api地址
     */
    @ApiModelProperty(value = "api地址")
    public String apiAddress;

    /**
     * api地址 目录
     */
    @ApiModelProperty(value = "api地址 目录")
    public String apiAddressCatalogue;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    public String apiDesc;

    /**
     * api描述 目录
     */
    @ApiModelProperty(value = "api描述 目录")
    public String apiDescCatalogue;

    /**
     * api请求类型
     * GET/POST
     */
    @ApiModelProperty(value = "api请求类型")
    public String apiRequestType;

    /**
     * api请求类型 目录
     */
    @ApiModelProperty(value = "api请求类型 目录")
    public String apiRequestTypeCatalogue;

    /**
     * apiContent-Type
     * application/json
     */
    @ApiModelProperty(value = "api接口内容类型")
    public String apiContentType;

    /**
     * apiContent-Type 目录
     */
    @ApiModelProperty(value = "api接口内容类型 目录")
    public String apiContentTypeCatalogue;

    /**
     * api Header
     * Authorization: Bearer {token}
     */
    @ApiModelProperty(value = "api标头")
    public String apiHeader;

    /**
     * apiHeader 目录
     */
    @ApiModelProperty(value = "api标头 目录")
    public String apiHeaderCatalogue;

    /**
     * API请求参数json示例
     */
    @ApiModelProperty(value = "api请求参数json示例")
    public String apiRequestExamples;

    /**
     * API请求参数json示例 目录
     */
    @ApiModelProperty(value = "api请求参数json示例 目录")
    public String apiRequestExamplesCatalogue;

    /**
     * API请求参数_固定部分
     */
    @ApiModelProperty(value = "api请求参数_固定部分")
    public List<ApiRequestDTO> apiRequestDTOS_Fixed;

    /**
     * API请求参数
     */
    @ApiModelProperty(value = "请求参数")
    public List<ApiRequestDTO> apiRequestDTOS;

    /**
     * API请求参数 目录
     */
    @ApiModelProperty(value = "api请求参数 目录")
    public String apiRequestCatalogue;

    /**
     * api唯一标识
     */
    @ApiModelProperty(value = "api唯一标识")
    public String apiUnique;

    /**
     * API返回参数json示例
     */
    @ApiModelProperty(value = "api返回参数json示例")
    public String apiResponseExamples;

    /**
     * API返回参数json示例 目录
     */
    @ApiModelProperty(value = "api返回参数json示例 目录")
    public String apiResponseExamplesCatalogue;

    /**
     * API返回参数
     */
    @ApiModelProperty(value = "api返回参数")
    public List<ApiResponseDTO> apiResponseDTOS;

    /**
     * API返回参数 目录
     */
    @ApiModelProperty(value = "api返回参数 目录")
    public String apiResponseCatalogue;

    /**
     * API返回参数 顶部描述
     */
    @ApiModelProperty(value = "api返回参数 顶部描述")
    public String apiResponseHeaderDesc;
}
