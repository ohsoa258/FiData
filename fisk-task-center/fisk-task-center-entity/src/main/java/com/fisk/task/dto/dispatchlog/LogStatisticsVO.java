package com.fisk.task.dto.dispatchlog;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Date;

@Data
public class LogStatisticsVO {
    @ApiModelProperty(value = "日期")
    public Date days;
    @ApiModelProperty(value = "数量")
    public int sum;

}
