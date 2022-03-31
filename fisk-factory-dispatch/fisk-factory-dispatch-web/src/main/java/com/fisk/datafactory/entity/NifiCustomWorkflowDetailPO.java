package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
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
    public Long pid;
    public String workflowId;
    public Integer componentsId;
    public String appId;
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
    public Integer schedule;
    public String script;
    /**
     * 左边指右边,左:outport,右:inport
     */
    public String inport;
    public String outport;
    public Boolean flag;
}
