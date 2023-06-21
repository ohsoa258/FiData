package com.fisk.dataservice.vo.atvserviceanalyse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API申请人明细：负责人创建的应用下绑定的API
 * @date 2023/6/19 11:53
 */
@Data
public class AtvApiPrincipalDetailAppBindApiVO {
    /**
     * app名称
     */
    @ApiModelProperty(value = "app名称")
    public String appName;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * api类型
     */
    @ApiModelProperty(value = "api类型")
    public String createApiType;

    /**
     * 申请人
     */
    @ApiModelProperty(value = "申请人")
    public String appPrincipal;

    /**
     * 申请时间
     */
    @ApiModelProperty(value = "申请时间")
    public String applyTime;
}
