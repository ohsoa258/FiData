package com.fisk.datafactory.dto.json;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/7 20:54
 */
@Data
public class TableJsonTargetDTO {

    @ApiModelProperty(value = "当前任务名称")
    public String taskName;

    public TableJsonChildDTO dto;

}
