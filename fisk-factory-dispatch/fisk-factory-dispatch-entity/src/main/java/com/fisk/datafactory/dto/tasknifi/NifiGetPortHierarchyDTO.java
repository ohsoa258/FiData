package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.enums.ChannelDataEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 1.3
 * @description nifi获取当前组件的层级关系要传的参数
 * @date 2022/2/21 16:56
 */
@Data
public class NifiGetPortHierarchyDTO {

    @ApiModelProperty(value = "管道名称", required = true)
    public String workflowName;

    @ApiModelProperty(value = "管道主键id")
    /**
     * 大管道的数字id
     */
    public String workflowId;

    @ApiModelProperty(value = "表类型",required = true)
    @NotNull
    public ChannelDataEnum channelDataEnum;

    @ApiModelProperty(value = "表id",required = true)
    @NotNull
    public String tableId;

    @ApiModelProperty(value = "当前组件id")
    public Long nifiCustomWorkflowDetailId;
}
