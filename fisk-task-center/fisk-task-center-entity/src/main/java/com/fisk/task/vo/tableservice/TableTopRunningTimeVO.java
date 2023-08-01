package com.fisk.task.vo.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-20
 * @Description:
 */
@Data
public class TableTopRunningTimeVO {
    @ApiModelProperty("表服务名称")
    public String tableServiceName;
    @ApiModelProperty("运行时间")
    public String runningTime;
}
