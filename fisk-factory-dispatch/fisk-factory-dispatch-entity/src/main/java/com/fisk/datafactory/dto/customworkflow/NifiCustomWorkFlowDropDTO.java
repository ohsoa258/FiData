package com.fisk.datafactory.dto.customworkflow;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class NifiCustomWorkFlowDropDTO {

    @ApiModelProperty(value = "id")
    public Integer id;

    @ApiModelProperty(value = "管道名称")
    public String workflowName;

}
