package com.fisk.task.dto.task;

import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BuildNifiFlowDTO extends MQBaseDTO {
    public Long id;
    public Long appId;
    public String tableName;
    /*
     * 默认pg,然后doris
     * */
    public SynchronousTypeEnum synchronousTypeEnum;
    /*
    * 表类别
    * */
    public OlapTableEnum type;
    /*
    * 数据来源类别
    * */
    public DataClassifyEnum dataClassifyEnum;

    /*
    * selectsql(只有toDoris,todw用得到)
    * */
    public String selectSql;
    /*
     * 同步方式(只有toDoris,todw用得到)
     * */
    public int synMode;

    /*
    * groupComponentId(管道,如果不为null,就子组里建立nifi流程)
    * */
    public String groupComponentId;
    /*
    * tb_nifi_custom_workflow_detail表的主键,用来区分同一张表被多次使用
    * */
    public String workflowDetailId;

    /*
    * 管道英文id(workflowId)
    * */
    public String nifiCustomWorkflowId;

    /*
    * 最大单位大组id
    * */
    public String groupStructureId;

    /*
     * 查询范围开始时间
     * */
    public String queryStartTime;

    /*
     * 查询范围结束时间
     * */
    public String queryEndTime;


}
