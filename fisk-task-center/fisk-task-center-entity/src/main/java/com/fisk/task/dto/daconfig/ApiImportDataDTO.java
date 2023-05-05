package com.fisk.task.dto.daconfig;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 * @version 1.3
 * @description
 * @date 2022/5/1 17:30
 */
@Data
public class ApiImportDataDTO extends MQBaseDTO {

    @ApiModelProperty(value = "应用Id")
    public Long appId;

    @ApiModelProperty(value = "接口Id")
    public Long apiId;

    @ApiModelProperty(value = "工作流Id")
    public String workflowId;

    @ApiModelProperty(value = "管道Api调度")
    public String pipelApiDispatch;
}
