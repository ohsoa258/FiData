package com.fisk.task.vo.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: wangjian
 * @Date: 2023-07-24
 * @Description:
 */
@Data
public class TableServiceDetailVO {
    @ApiModelProperty("表服务名称")
    public String tableServiceName;

    @ApiModelProperty("开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date startDateTime;

    @ApiModelProperty("结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date endDateTime;

    @ApiModelProperty("运行时长")
    public String runningTime;
    @ApiModelProperty("执行状态")
    public String runningStatus;
    @ApiModelProperty("执行结果")
    public String runningResult;
}
