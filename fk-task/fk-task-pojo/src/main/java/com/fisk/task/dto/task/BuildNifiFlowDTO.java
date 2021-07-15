package com.fisk.task.dto.task;

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
}
