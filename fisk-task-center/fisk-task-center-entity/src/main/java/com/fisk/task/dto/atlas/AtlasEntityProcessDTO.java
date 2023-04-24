package com.fisk.task.dto.atlas;


import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/15 11:42
 * Description:
 */
public class AtlasEntityProcessDTO {
    @ApiModelProperty(value = "创建者")
    public String createUser;
    @ApiModelProperty(value = "进程名")
    public String processName;
    @ApiModelProperty(value = "更高类型")
    public String higherType;
    @ApiModelProperty(value = "des")
    public String des;

    @ApiModelProperty(value = "限定名")
    public String qualifiedName;
}
