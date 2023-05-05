package com.fisk.task.po.app;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_app_nifi_setting")
public class AppNifiSettingPO extends BasePO {
    public String appPid;
    public String appId;
    public String appComponentId;
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
