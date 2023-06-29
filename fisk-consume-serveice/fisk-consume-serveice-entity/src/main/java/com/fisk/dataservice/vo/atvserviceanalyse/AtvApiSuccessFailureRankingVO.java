package com.fisk.dataservice.vo.atvserviceanalyse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API当天调用趋势：接口调用成功失败的数量排名
 * @date 2023/6/19 11:53
 */
@Data
public class AtvApiSuccessFailureRankingVO {
    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * 日期节点
     */
    @ApiModelProperty(value = "日期节点")
    public String dateSlot;

    /**
     * 时间节点
     */
    @ApiModelProperty(value = "时间节点")
    public String timeSlot;

    /**
     * 调用成功次数
     */
    @ApiModelProperty(value = "调用成功次数")
    public String successCount;

    /**
     * 调用失败次数
     */
    @ApiModelProperty(value = "调用失败次数")
    public String failureCount;
}
