package com.fisk.task.dto.datagovernance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 报告DTO
 * @date 2022/8/23 16:54
 */
@Data
public class ReportDTO {

    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "用户id")
    public Long userId;
}
