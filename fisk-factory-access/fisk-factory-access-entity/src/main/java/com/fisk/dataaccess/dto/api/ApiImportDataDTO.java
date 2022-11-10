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

    @ApiModelProperty(value = "管道task_id", required = true)
    public String workflowId;

    @ApiModelProperty(value = "管道id_apiid", required = true)
    public String pipelApiDispatch;

    @ApiModelProperty(value = "管道批次id", required = true)
    public String pipelTraceId;
    /*
     * job批次号
     * */
    public String pipelJobTraceId;

    /*
     * task批次号
     * */
    public String pipelTaskTraceId;

    /*
     * stage批次号
     * */
    public String pipelStageTraceId;

    public long userId;


}
