package com.fisk.task.dto.mdmtask;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author wangjian
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BuildMdmNifiFlowDTO extends MQBaseDTO {
    @ApiModelProperty(value = "id")
    public Long id;
    @ApiModelProperty(value = "accessId")
    public Long accessId;
    @ApiModelProperty(value = "modelId")
    public Long modelId;
    @ApiModelProperty(value = "entityId")
    public Long entityId;
    @ApiModelProperty(value = "版本id")
    public Integer versionId;
    @ApiModelProperty(value = "表名")
    public String tableName;
    @ApiModelProperty(value = "默认OdsToMdm")
    public SynchronousTypeEnum synchronousTypeEnum;
    @ApiModelProperty(value = "表类别")
    public OlapTableEnum type;
    @ApiModelProperty(value = "数据来源类别")
    public DataClassifyEnum dataClassifyEnum;
    @ApiModelProperty(value = "源表查询sql")
    public String selectSql;
    @ApiModelProperty(value = "同步方式")
    public int synMode;
    @ApiModelProperty(value = "groupComponentId(管道,如果不为null,就子组里建立nifi流程)")
    public String groupComponentId;
    @ApiModelProperty(value = "tb_nifi_custom_workflow_detail表的主键,用来区分同一张表被多次使用")
    public String workflowDetailId;
    @ApiModelProperty(value = "管道英文id(workflowId)")
    public String nifiCustomWorkflowId;
    @ApiModelProperty(value = "最大单位大组id")
    public String groupStructureId;
    @ApiModelProperty(value = "查询范围开始时间")
    public String queryStartTime;
    @ApiModelProperty(value = "查询范围结束时间")
    public String queryEndTime;
    @ApiModelProperty(value = "模型名称")
    public String modelName;
    @ApiModelProperty(value = "entity名称")
    public String entityName;
    @ApiModelProperty(value = "是否同步")
    public Boolean openTransmission;
    @ApiModelProperty(value = "是否是表格同步")
    public boolean excelFlow;
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;
    @ApiModelProperty(value = "版本字段语句")
    public String generateVersionSql;
    @ApiModelProperty(value = "单个数据流文件加载最大数据行")
    public int maxRowsPerFlowFile;
    @ApiModelProperty(value = "单次从结果集中提取的最大数据行")
    public int fetchSize;
    @ApiModelProperty(value = "数据来源id")
    public Integer dataSourceDbId;
    @ApiModelProperty(value = "目标数据源id")
    public Integer targetDbId;
    @ApiModelProperty(value = "自定义脚本执行前")
    public String customScriptBefore;
    @ApiModelProperty(value = "自定义脚本执行后")
    public String customScriptAfter;
    @ApiModelProperty(value = "从stg抽取数据同步到mdm的sql语句")
    public String syncStgToMdmSql;
    @ApiModelProperty(value = "发布历史id")
    public Long tableHistoryId;


}
