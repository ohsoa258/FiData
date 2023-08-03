package com.fisk.task.vo.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-20
 * @Description:
 */
@Data
public class TableFaildStatisticsVO {
    @ApiModelProperty("表服务名称")
    public String tableServiceName;
    @ApiModelProperty("总执行次数")
    public int sum;
    @ApiModelProperty("成功次数")
    public int successNum;
    @ApiModelProperty("失败次数")
    public int faildNum;
    @ApiModelProperty("成功率")
    public double success;
    @ApiModelProperty("失败率")
    public double faild;
}
