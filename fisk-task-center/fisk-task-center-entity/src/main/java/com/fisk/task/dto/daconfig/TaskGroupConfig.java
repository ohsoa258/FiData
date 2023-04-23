package com.fisk.task.dto.daconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class TaskGroupConfig {
    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "应用名称")
    public String appName;

    @ApiModelProperty(value = "应用详细信息")
    public String appDetails;

    /**
     * 组件id
     */
    @ApiModelProperty(value = "组件id")
    public String componentId;

    /*
    * 是否是新的任务组
    * */
    @ApiModelProperty(value = "是否是新的任务组")
    public Boolean isNewTask;
}
