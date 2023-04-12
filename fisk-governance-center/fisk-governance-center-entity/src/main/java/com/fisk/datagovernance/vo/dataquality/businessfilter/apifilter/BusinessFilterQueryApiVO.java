package com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BusinessFilterQueryApiVO {
    /**
     * 清洗规则ID
     */
    @ApiModelProperty(value = "清洗规则ID")
    public Integer ruleId;

    /**
     * API基础信息配置
     */
    @ApiModelProperty(value = "API基础信息配置")
    public BusinessFilterApiConfigVO apiConfig;

    /**
     * API请求参数配置
     */
    @ApiModelProperty(value = "API请求参数配置")
    public List<BusinessFilterApiParamVO> apiParamConfig;

    /**
     * API返回结果配置
     */
    @ApiModelProperty(value = "API返回结果配置")
    public List<BusinessFilterApiResultVO> apiResultConfig;
}
