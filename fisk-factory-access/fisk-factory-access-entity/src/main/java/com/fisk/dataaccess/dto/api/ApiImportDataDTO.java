package com.fisk.dataaccess.dto.api;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/1 17:30
 */
@Data
public class ApiImportDataDTO {

    @NotNull
    @ApiModelProperty(value = "应用id", required = true)
    public long appId;

    @NotNull
    @ApiModelProperty(value = "非实时apiId", required = true)
    public long apiId;

    @ApiModelProperty(value = "管道id", required = true)
    public String workflowId;

    @ApiModelProperty(value = "管道id_apiid", required = true)
    public String workflowIdAppIdApiId;

    public long userId;

}
