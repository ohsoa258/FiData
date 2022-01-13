package com.fisk.dataservice.vo.app;

import com.fisk.dataservice.enums.ApiStateTypeEnum;
import io.swagger.annotations.ApiModelProperty;


/**
 * @author dick
 * @version v1.0
 * @description 应用订阅API VO
 * @date 2022/1/10 17:51
 */
public class AppApiSubVO {
    /**
     * Id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public Integer appId;

    /**
     * API id
     */
    @ApiModelProperty(value = "API id")
    public Integer apiId;

    /**
     * API 状态 1启用、0禁用
     */
    @ApiModelProperty(value = "apiState")
    public ApiStateTypeEnum apiState;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * api标识
     */
    @ApiModelProperty(value = "api标识")
    public String apiCode;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    public String apiDesc;
}
