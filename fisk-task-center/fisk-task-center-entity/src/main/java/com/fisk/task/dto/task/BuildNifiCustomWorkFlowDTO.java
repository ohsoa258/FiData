package com.fisk.task.dto.task;

import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class BuildNifiCustomWorkFlowDTO {


    //操作类型
    @ApiModelProperty(value = "操作类型")
    public DataClassifyEnum type;
    //appid或者业务域id(对应前面的接入或建模id或任务组时,就是任务组id)
    // 只有是任务组时,赋值 tb_nifi_custom_workflow_detail表 id
    @ApiModelProperty(value = "appid或者业务域id(对应前面的接入或建模id或任务组时,就是任务组id)")
    public Long appId;
    //物理or事实or维度or指标表表id(对应事实表,物理表,维度表,指标表id)
    @ApiModelProperty(value = "物理or事实or维度or指标表表id(对应事实表,物理表,维度表,指标表id)")
    public String tableId;

    //表类别
    @ApiModelProperty(value = "表类别")
    public OlapTableEnum tableType;
    //表名(为空)
    @ApiModelProperty(value = "表名(为空)")
    public String tableName;
    //组id(当前组件pid)
    @ApiModelProperty(value = "组id(当前组件pid)")
    public String groupId;

    // 后四个属性只有调度任务才有
    //组件名称(管道名称)
    @ApiModelProperty(value = "表类别")
    public String nifiCustomWorkflowName;

    //组件id(八类组件表里的id  详情表id)
    @ApiModelProperty(value = "组件id(八类组件表里的id  详情表id)")
    public Long nifiCustomWorkflowId;
    //调度频率
    @ApiModelProperty(value = "调度频率")
    public String scheduleExpression;
    //调度方式
    @ApiModelProperty(value = "调度方式")
    public SchedulingStrategyTypeEnum scheduleType;
    // 管道详情表id
    @ApiModelProperty(value = "管道详情表id")
    public Long workflowDetailId;
}
