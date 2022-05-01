package com.fisk.datafactory.dto.customworkflowdetail;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowDetailDTO {

    public long id;
    public Long pid;
    public String workflowId;
    @ApiModelProperty(value = "左边组件类型id", required = true)
    public Integer componentsId;
    @ApiModelProperty(value = "应用id or 业务域id")
    public String appId;
    @ApiModelProperty(value = "表id")
    public String tableId;
    public String appName;
    public String tableName;
    /**
     * 常规: 名称
     */
    @ApiModelProperty(value = "左边组件名称", required = true)
    public String componentName;
    /**
     * 常规: 类型
     */
    @ApiModelProperty(value = "具体的组件名称,如数仓下的数仓维度表任务,数仓事实表任务等", required = true)
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
