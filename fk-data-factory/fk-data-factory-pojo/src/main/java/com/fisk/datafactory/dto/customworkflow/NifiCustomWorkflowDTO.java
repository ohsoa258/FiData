package com.fisk.datafactory.dto.customworkflow;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowDTO {

    public long id;
    public String workflowId;
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
    public int status;
}
