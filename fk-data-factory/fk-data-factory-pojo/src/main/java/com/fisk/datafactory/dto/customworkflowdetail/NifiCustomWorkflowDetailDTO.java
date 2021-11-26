package com.fisk.datafactory.dto.customworkflowdetail;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowDetailDTO {

    public long id;
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
     *  元数据对象
     */
    public String metadataObj;
    public Integer schedule;
    public String script;
    public String inport;
    public String outport;
    public Boolean flag;
}
