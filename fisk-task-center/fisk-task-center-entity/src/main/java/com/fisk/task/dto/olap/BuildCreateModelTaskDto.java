package com.fisk.task.dto.olap;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;

/**
 * 创建模型TaskDto
 * @author JinXingWang
 */
public class BuildCreateModelTaskDto  extends MQBaseDTO {
    @ApiModelProperty(value = "业务域Id")
    public int businessAreaId;
}
