package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description 组件层级关系对象
 * @date 2022/2/16 15:26
 */
@Data
public class NifiPortsHierarchyDTO {

    @ApiModelProperty(value = "当前task属性")
    public NifiCustomWorkflowDetailDTO itselfPort;

    @ApiModelProperty(value = "是否为所属job中的第一个任务")
    public boolean componentFirstFlag;
    @ApiModelProperty(value = "是否为所属job中的最后一个任务")
    public boolean componentEndFlag;
    @ApiModelProperty(value = "是否为管道内最后一个任务")
    public boolean pipeEndFlag;
    /**
     * 管道每个分支的最后一个task
     */
    public List<NifiCustomWorkflowDetailDTO> pipeEndDto;

    /**
     * 当前task的所有下一级task集合
     */
    public List<NifiPortsHierarchyNextDTO> nextList;

    /**
     * 当前任务的上一级主任务中的最后一个表任务集合
     */
    public List<NifiCustomWorkflowDetailDTO> inportList;
}
