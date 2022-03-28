package com.fisk.task.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_nifi_scheduling_component")
public class NifiSchedulingComponentPO extends BasePO {

    public String name;
    public String componentId;
    public String groupComponentId;
    public String inputDucts;
    public String outputDucts;
    public String nifiCustomWorkflowDetailId;
    public String inputPortId;
    public String outputPortId;
    public String inputPortConnectId;
    public String outputPortConnectId;

}
