package com.fisk.task.dto.accessmodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author wangjian
 */
@Data
public class AccessMdmPublishTableDTO {
    @ApiModelProperty(value = "表Id")
    public long tableId;
    @ApiModelProperty(value = "表名称")
    public String tableName;
    /**
     * 创建表方式 2:维度 1:事实 3: 数据接入 4:mdm
     */
    @ApiModelProperty(value = "创建表方式 2:维度 1:事实 3: 数据接入 4:mdm")
    public int createType;
    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;
    @ApiModelProperty(value = "字段列表")
    public List<AccessMdmPublishFieldDTO> fieldList;
    @ApiModelProperty(value = "组组件Id")
    public String groupComponentId;
    @ApiModelProperty(value = "nifi自定义工作流详细Id")
    public String nifiCustomWorkflowDetailId;

    /*
     * 查询范围开始时间
     * */
    @ApiModelProperty(value = "查询范围开始时间")
    public String queryStartTime;

    /*
     * 查询范围结束时间
     * */
    @ApiModelProperty(value = "查询范围结束时间")
    public String queryEndTime;

    /*
    * 同步方式
    * */
    @ApiModelProperty(value = "同步模式")
    public Integer synMode;

    @ApiModelProperty(value = "组结构id")
    public String groupStructureId;
    /**
     * 单个数据流文件加载最大数据行
     */
    @ApiModelProperty(value = "单个数据流文件加载最大数据行")
    public int maxRowsPerFlowFile;
    /**
     * 单次从结果集中提取的最大数据行
     */
    @ApiModelProperty(value = "单次从结果集中提取的最大数据行")
    public int fetchSize;
    /**
     * 自定义脚本
     */
    @ApiModelProperty(value = "自定义脚本")
    public String customScript;

    /**
     * 自定义脚本最后执行
     */
    @ApiModelProperty(value = "自定义脚本最后执行")
    public String customScriptAfter;

    /**
     * 目标源id
     */
    @ApiModelProperty(value = "目标源id")
    public Integer targetDbId;

    /**
     * 来源id
     */
    @ApiModelProperty(value = "来源id")
    public Integer dataSourceDbId;

    /**
     * 覆盖脚本执行SQL语句
     */
    @ApiModelProperty(value = "覆盖脚本执行SQL语句")
    public String coverScript;
}
