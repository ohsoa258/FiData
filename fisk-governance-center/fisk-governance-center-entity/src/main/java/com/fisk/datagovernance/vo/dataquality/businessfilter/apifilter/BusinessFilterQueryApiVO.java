package com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/8 16:53
 */
@Data
public class BusinessFilterQueryApiVO {
    /**
     * API基础信息配置
     */
    @ApiModelProperty(value = "API基础信息配置")
    public BusinessFilterApiConfigVO apiConfig;

    /**
     * API请求参数配置
     */
    @ApiModelProperty(value = "API请求参数配置")
    public List<BusinessFilterApiParmVO> apiParmConfig;

    /**
     * API返回结果配置
     */
    @ApiModelProperty(value = "API返回结果配置")
    public List<BusinessFilterApiResultVO> apiResultConfig;
}
