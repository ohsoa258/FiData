package com.fisk.datafactory.vo.customworkflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

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
    @ApiModelProperty(value = "状态：已发布（1）、未发布（0）、发布失败（2）、正在发布（3）", required = true)
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
}
