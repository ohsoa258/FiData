package com.fisk.datafactory.vo.customworkflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lock
 *
 * 管道分页对象
 */
@Data
public class NifiCustomWorkflowVO {

    public long id;
    public String workflowId;
    /**
     * 管道名称
     */
    public String workflowName;
    /**
     * 负责人
     */
    public String pr;
    /**
     * 描述
     */
    public String desc;
    /**
     * 组件节点
     */
    public String listNode;
    /**
     * 组件、连线
     */
    public String listEdge;
    /**
     * 是否发布
     */
    public int status;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime updateTime;

}
