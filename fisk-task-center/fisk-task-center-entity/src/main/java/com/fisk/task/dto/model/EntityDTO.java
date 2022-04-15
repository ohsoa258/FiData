package com.fisk.task.dto.model;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/4/13 10:10
 */
@Data
public class EntityDTO extends MQBaseDTO {

    /**
     * 实体id
     */
    private String entityId;
}
