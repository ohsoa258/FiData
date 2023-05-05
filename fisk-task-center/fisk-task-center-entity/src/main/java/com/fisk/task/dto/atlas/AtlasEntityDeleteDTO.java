package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 17:55
 * Description:
 */
@Data
public class AtlasEntityDeleteDTO extends MQBaseDTO {
    @ApiModelProperty(value = "实体Id")
    public String entityId;
}
