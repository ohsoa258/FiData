package com.fisk.task.dto.task;

import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.task.dto.MQBaseDTO;
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
}
