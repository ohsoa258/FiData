package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 应用下服务的数量
 * @date 2023/8/31 10:36
 */
@Data
public class AppServiceCountVO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public int appId;

    /**
     * 应用下服务的数量
     */
    @ApiModelProperty(value = "应用下服务的数量")
    public int count;
}
