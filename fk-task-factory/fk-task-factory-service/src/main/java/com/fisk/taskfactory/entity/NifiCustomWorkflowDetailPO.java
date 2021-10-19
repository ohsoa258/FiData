package com.fisk.taskfactory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
@TableName("tb_nifi_custom_workflow_detail")
public class NifiCustomWorkflowDetailPO extends BasePO {

    public int workflowId;
    public int componentsId;
    public String tableId;
    public int schedule;
    public String script;
    public String inport;
    public String outport;
}
