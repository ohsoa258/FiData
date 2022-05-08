package com.fisk.datafactory.dto.json;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/7 20:56
 */
@Data
public class TableJsonSourceDTO {

    @ApiModelProperty(value = "当前任务id", required = true)
    @NotNull
    public long id;

    @ApiModelProperty(value = "当前任务类型", required = true)
    public String componentType;
}
