package com.fisk.task.vo.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-18
 * @Description:
 */
@Data
public class TableStatisticsVO {
    @ApiModelProperty(value = "成功数量")
    public Integer successSum;
    @ApiModelProperty(value = "失败数量")
    public Integer failureSum;
    @ApiModelProperty(value = "正在运行数量")
    public Integer runningSum;
}
