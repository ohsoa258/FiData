package com.fisk.datafactory.dto.customworkflow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * @author SongJianJian
 */
@Data
public class NifiCustomWorkflowUpdateDTO {

    @ApiModelProperty(value = "管道id", required = true)
    @NotEmpty(message = "管道id不能为空")
    private String nifiCustomWorkflowId;

    @ApiModelProperty(value = "管道工作状态（true：开始，false：暂停）", required = true)
    @NotNull(message = "管道工作状态不能为空")
    private Boolean ifFire;
}
