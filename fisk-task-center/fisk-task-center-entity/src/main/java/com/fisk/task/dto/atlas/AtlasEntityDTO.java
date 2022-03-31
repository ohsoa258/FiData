package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/9 17:34
 * Description:
 */
@Data
public class AtlasEntityDTO extends MQBaseDTO {
    public String appName;
    public String driveType;
    public String createUser;
    public String appDes;
    public String host;
    public String port;
    public String dbName;
}
