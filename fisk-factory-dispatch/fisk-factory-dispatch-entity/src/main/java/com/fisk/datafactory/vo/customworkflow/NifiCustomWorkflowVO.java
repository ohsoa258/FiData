package com.fisk.datafactory.vo.customworkflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lock
 *
 * 管道分页对象
 */
@Data
public class NifiCustomWorkflowVO {

    @ApiModelProperty(value = "管道id")
    public long id;
    @ApiModelProperty(value = "管道guid")
    public String workflowId;
    @ApiModelProperty(value = "管道名称", required = true)
    public String workflowName;
    /**
     * 负责人
     */
    @ApiModelProperty(value = "管道负责人", required = true)
    public String pr;
    /**
     * 描述
     */
    @ApiModelProperty(value = "管道描述")
    public String desc;
    /**
     * 组件节点
     */
    @ApiModelProperty(value = "组件节点")
    public String listNode;
    /**
     * 组件、连线
     */
    @ApiModelProperty(value = "组件连线", required = true)
    public String listEdge;
    @ApiModelProperty(value = "发布状态：已发布（1）、未发布（0）、发布失败（2）、正在发布（3）", required = true)
    public int status;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    public LocalDateTime updateTime;

    @ApiModelProperty(value = "呼吸灯状态(0/1:未运行  2:正在运行  3:运行成功 4:运行失败)")
    public int breathingLamp;

    @ApiModelProperty(value = "管道内绑定表的组件id集合")
    public List<Long> componentIds;
}
