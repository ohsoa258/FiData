package com.fisk.task.vo.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-20
 * @Description:
 */
@Data
public class TableServiceLineChartVO {
    @ApiModelProperty("表服务名称")
    public String tableServiceName;
    @ApiModelProperty("日期")
    public String date;
    @ApiModelProperty("运行时长")
    public String runningTime;
}
