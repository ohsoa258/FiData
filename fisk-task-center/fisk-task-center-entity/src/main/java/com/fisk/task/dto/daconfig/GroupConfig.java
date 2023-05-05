package com.fisk.task.dto.daconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class GroupConfig {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "应用名称")
    public String appName;
    @ApiModelProperty(value = "应用详细信息")
    public String appDetails;

    /**
     * 是否需要创建新的项目
     */
    @ApiModelProperty(value = "是否需要创建新的项目")
    public boolean newApp;

    /**
     * 组件id
     */
    @ApiModelProperty(value = "组件id")
    public String componentId;
}
