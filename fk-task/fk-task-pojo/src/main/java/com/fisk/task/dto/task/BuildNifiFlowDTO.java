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
    * selectsql(只有toDoris用得到)
    * */
    public String selectSql;

    /*
    * groupComponentId(管道,如果不为null,就子组里建立nifi流程)
    * */
    public String groupComponentId;
}
