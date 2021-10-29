package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
@TableName("tb_nifi_custom_workflow_detail")
public class NifiCustomWorkflowDetailPO extends BasePO {
    /**
     * 父组件
     */
    public long pid;
    public String workflowId;
    public int componentsId;
    public String tableId;
    /**
     * 常规: 名称
     */
    public String componentName;
    /**
     * 常规: 类型
     */
    public String componentType;
    /**
     * 常规: 描述
     */
    public String componentDesc;
    public Double componentX;
    public Double componentY;
    /**
     * 元数据对象
     */
    public String metadataObj;
    public int schedule;
    public String script;
    public String inport;
    public String outport;
}
