package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/14 12:58
 * Description:
 */
@Data
public class AtlasEntityQueryDTO extends MQBaseDTO {
    public String appId;
    public String dbId;
    public String tableName;
}
