package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 * @version 1.3
 * @description 组件层级关系对象
 * @date 2022/2/16 15:26
 */
@Data
public class NifiPortsHierarchyDTO {

    @ApiModelProperty(value = "task的主键id")
    public Long id;

    @ApiModelProperty(value = "管道主键id")
    public Long pipelineId;

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
     * 当前task的所有上一级task集合
     */
    public List<NifiCustomWorkflowDetailDTO> inportList;
    /**
     * 本节点特殊参数
     */
    public Map<String, String> specialParaMap;

    /**
     * 本节点状态类参数
     */
    public Map<String, String> taskParaMap;


}
