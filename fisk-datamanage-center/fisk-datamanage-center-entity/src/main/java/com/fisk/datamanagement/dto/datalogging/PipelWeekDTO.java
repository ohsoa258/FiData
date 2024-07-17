package com.fisk.datamanagement.dto.datalogging;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2024-07-16
 * @Description:
 */
@Data
public class PipelWeekDTO {
    @ApiModelProperty(value = "当天运行次数")
    private Integer totalCount;
    @ApiModelProperty(value = "日期")
    private LocalDateTime date;
}
