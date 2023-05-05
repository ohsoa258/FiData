package com.fisk.datafactory.dto.customworkflowdetail;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 *     管道任务组
 * </p>
 * @author Lock
 */
@Data
public class WorkflowTaskGroupDTO {
    /**
     * 管道任务组集合
     */
    @ApiModelProperty(value = "管道任务组集合")
    public List<NifiCustomWorkflowDetailDTO> list;
}
