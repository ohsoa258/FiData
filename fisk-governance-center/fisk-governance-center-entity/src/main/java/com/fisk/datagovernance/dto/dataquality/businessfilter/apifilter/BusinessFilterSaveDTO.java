package com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗保存配置DTO
 * @date 2022/10/8 16:23
 */
@Data
public class BusinessFilterSaveDTO {
    /**
     * API基础信息配置
     */
    @ApiModelProperty(value = "API基础信息配置")
    public BusinessFilterApiConfigDTO apiConfig;

    /**
     * API请求参数配置
     */
    @ApiModelProperty(value = "API请求参数配置")
    public List<BusinessFilterApiParmDTO> apiParmConfig;

    /**
     * API返回结果配置
     */
    @ApiModelProperty(value = "API返回结果配置")
    public List<BusinessFilterApiResultDTO> apiResultConfig;
}
