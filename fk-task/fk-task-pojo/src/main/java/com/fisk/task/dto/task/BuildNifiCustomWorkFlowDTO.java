package com.fisk.task.dto.task;

import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;


@Data
public class BuildNifiCustomWorkFlowDTO {

    //操作类型
    public DataClassifyEnum type;
    //appid或者业务域id(对应前面的接入或建模id)
    public Integer appId;
    //物理or事实or维度or指标表表id(对应事实表,物理表,维度表,指标表id)
    public Integer tableId;
    //表类别
    public OlapTableEnum tableType;
    //表名
    public String tableName;
    //组id
    public Integer groupId;
    //组件名称
    public String nifiCustomWorkflowName;
    //组件id(八类组件表里的id)
    public Integer nifiCustomWorkflowId;
    //调度频率
    public String scheduleExpression;
    //调度方式
    public SchedulingStrategyTypeEnum scheduleType;


}
