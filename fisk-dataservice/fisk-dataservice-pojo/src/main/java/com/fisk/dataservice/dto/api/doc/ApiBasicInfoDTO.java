package com.fisk.dataservice.dto.api.doc;

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
    public String apiName;

    /**
     * api名称 目录
     */
    public String apiNameCatalogue;

    /**
     * api地址
     */
    public String apiAddress;

    /**
     * api地址 目录
     */
    public String apiAddressCatalogue;

    /**
     * api描述
     */
    public String apiDesc;

    /**
     * api描述 目录
     */
    public String apiDescCatalogue;

    /**
     * api请求类型
     * GET/POST
     */
    public String apiRequestType;

    /**
     * api请求类型 目录
     */
    public String apiRequestTypeCatalogue;

    /**
     * apiContent-Type
     * application/json
     */
    public String apiContentType;

    /**
     * apiContent-Type 目录
     */
    public String apiContentTypeCatalogue;

    /**
     * api Header
     * Authorization: Bearer {token}
     */
    public String apiHeader;

    /**
     * apiHeader 目录
     */
    public String apiHeaderCatalogue;

    /**
     * API请求参数
     */
    public List<ApiRequestDTO> apiRequestDTOS;

    /**
     * API请求参数 目录
     */
    public String apiRequestCatalogue;

    /**
     * API返回参数json示例
     */
    public String apiResponseExamples;

    /**
     * API返回参数json示例 目录
     */
    public String apiResponseExamplesCatalogue;

    /**
     * API返回参数
     */
    public List<ApiResponseDTO> apiResponseDTOS;

    /**
     * API返回参数 目录
     */
    public String apiResponseCatalogue;
}
