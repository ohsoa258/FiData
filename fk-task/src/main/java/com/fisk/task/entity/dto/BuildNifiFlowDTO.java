package com.fisk.task.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BuildNifiFlowDTO extends MQBaseDTO{
    public Long id;
}
