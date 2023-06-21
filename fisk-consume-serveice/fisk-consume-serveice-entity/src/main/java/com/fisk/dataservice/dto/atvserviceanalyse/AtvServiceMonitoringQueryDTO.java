package com.fisk.dataservice.dto.atvserviceanalyse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 服务监控请求参数DTO
 * @date 2023/6/19 15:22
 */
@Data
public class AtvServiceMonitoringQueryDTO {
    /**
     * API ID
     */
    @ApiModelProperty(value = "apiId")
    public int apiId;

    /**
     * APP ID
     */
    @ApiModelProperty(value = "appId")
    public int appId;

    /**
     * createApiType
     * 1 创建新api、2 使用现有api、3 代理API
     */
    @ApiModelProperty(value = "createApiType：1 创建新api、2 使用现有api、3 代理API")
    public int createApiType;
}
