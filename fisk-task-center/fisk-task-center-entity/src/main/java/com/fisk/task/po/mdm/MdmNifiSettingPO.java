package com.fisk.task.po.mdm;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wangjian
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_mdm_nifi_setting")
public class MdmNifiSettingPO extends BasePO {
    public String modelPid;
    public String modelId;
    public String modelComponentId;
    public String targetDbPoolComponentId;
    public String sourceDbPoolComponentId;
    public int type;
    public String inputPortId;
    public String outputPortId;
    public String inputDucts;
    public String outputDucts;
    public String inputPortConnectId;
    public String outputPortConnectId;
    public String nifiCustomWorkflowId;
}
