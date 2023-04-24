package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/9 17:34
 * Description:
 */
@Data
public class AtlasEntityDTO extends MQBaseDTO {
    @ApiModelProperty(value = "应用名称")
    public String appName;
    @ApiModelProperty(value = "驱动类型")
    public String driveType;
    @ApiModelProperty(value = "创建者")
    public String createUser;
    @ApiModelProperty(value = "应用详细信息")
    public String appDes;
    @ApiModelProperty(value = "主机")
    public String host;
    @ApiModelProperty(value = "端口")
    public String port;
    @ApiModelProperty(value = "数据库名称")
    public String dbName;
}
