package com.fisk.datafactory.dto.customworkflowdetail;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowDetailDTO {

    @ApiModelProperty(value = "id")
    public long id;
    @ApiModelProperty(value = "pid")
    public Long pid;
    @ApiModelProperty(value = "管道id")
    public String workflowId;
    @ApiModelProperty(value = "管道名称")
    public String workflowName;
    @ApiModelProperty(value = "左边组件类型id", required = true)
    public Integer componentsId;
    @ApiModelProperty(value = "task组件名称")
    public String componentsName;
    @ApiModelProperty(value = "应用id or 业务域id")
    public String appId;
    @ApiModelProperty(value = "表id")
    public String tableId;
    @ApiModelProperty(value = "组件绑定的表的顺序")
    public Integer tableOrder;
    @ApiModelProperty(value = "应用名称")
    public String appName;
    @ApiModelProperty(value = "表名称")
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
    @ApiModelProperty(value = "组件详细信息")
    public String componentDesc;

    @ApiModelProperty(value = "组件X")
    public Double componentX;

    @ApiModelProperty(value = "组件Y")
    public Double componentY;
    /**
     * 元数据对象
     */
    @ApiModelProperty(value = "元数据项目")
    public String metadataObj;
    @ApiModelProperty(value = "计划表")
    public Integer schedule;
    @ApiModelProperty(value = "脚本")
    public String script;
    @ApiModelProperty(value = "输入")
    public String inport;
    @ApiModelProperty(value = "输出")
    public String outport;
    @ApiModelProperty(value = "标记")
    public Boolean flag;
    /**
     * 是否禁用,true不禁用,false 禁用
     */
    @ApiModelProperty(value = "是否禁用,true不禁用,false 禁用")
    public Boolean forbidden;

    @ApiModelProperty(value = "外部数据源id", required = true)
    public Integer dataSourceId;

    @ApiModelProperty(value = "自定义脚本任务", required = true)
    public String customScript;

    @ApiModelProperty(value = "组件参数", required = true)
    public Map<String,String> taskSetting;

}
