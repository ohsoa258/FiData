package com.fisk.task.dto.model;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "实体id")
    private Integer entityId;
}
