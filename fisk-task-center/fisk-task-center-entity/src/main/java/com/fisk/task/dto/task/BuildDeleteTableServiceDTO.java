package com.fisk.task.dto.task;

import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

import java.util.List;

/**
 * @author cfk
 */
@Data
public class BuildDeleteTableServiceDTO extends MQBaseDTO {
    /**
     * 表类别
     */
    public OlapTableEnum olapTableEnum;
    /**
     * 表id集合
     */
    public List<Long> ids;
}
