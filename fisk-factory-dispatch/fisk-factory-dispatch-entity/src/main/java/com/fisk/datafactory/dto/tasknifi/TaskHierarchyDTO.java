package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.task.enums.DispatchLogEnum;
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
public class TaskHierarchyDTO {

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
     * 要变成task_id  NifiCustomWorkflowDetailDTO
     * 管道每个分支的最后一个task
     */
    @ApiModelProperty(value = "管道每个分支的最后一个task")
    public List<Long> pipeEndDto;
    /**
     * 也要变成task_id  NifiPortsHierarchyNextDTO
     * 当前task的所有下一级task集合
     */
    @ApiModelProperty(value = "当前task的所有下一级task集合")
    public List<NifiPortsHierarchyNextDTO> nextList;
    /**
     * 要变成id   NifiCustomWorkflowDetailDTO
     * 当前task的所有上一级task集合
     */
    @ApiModelProperty(value = "当前task的所有上一级task集合")
    public List<Long> inportList;
    /**
     * 本节点特殊参数
     */
    public Map<String, String> specialParaMap;

    /**
     * task运行状态,包括 未运行,运行成功,运行失败
     */
    public DispatchLogEnum taskStatus;
    /**
     * job处理状态.true 已处理. false 未处理
     */
    public boolean taskProcessed;


}
