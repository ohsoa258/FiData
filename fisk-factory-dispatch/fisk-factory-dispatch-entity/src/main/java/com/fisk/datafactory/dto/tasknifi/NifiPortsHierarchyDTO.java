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

    @ApiModelProperty(value = "当前组件属性")
    public NifiCustomWorkflowDetailDTO itselfPort;

    @ApiModelProperty(value = "是否为所属组件中的第一个任务")
    public boolean componentFirstFlag;
    @ApiModelProperty(value = "是否为所属组件中的最后一个任务")
    public boolean componentEndFlag;
    @ApiModelProperty(value = "是否为管道内最后一个任务")
    public boolean pipeEndFlag;
    /**
     * 管道流程最后一批集合
     */
    public List<NifiCustomWorkflowDetailDTO> pipeEndDto;

    /**
     * 下一级组件集合
     */
    public List<NifiPortsHierarchyNextDTO> nextList;
}
