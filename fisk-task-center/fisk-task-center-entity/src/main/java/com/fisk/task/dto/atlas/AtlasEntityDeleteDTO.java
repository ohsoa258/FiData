package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 17:55
 * Description:
 */
@Data
public class AtlasEntityDeleteDTO extends MQBaseDTO {
    public String entityId;
}
