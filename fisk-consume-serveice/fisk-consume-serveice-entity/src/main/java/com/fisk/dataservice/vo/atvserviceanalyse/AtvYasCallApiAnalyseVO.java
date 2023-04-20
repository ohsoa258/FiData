package com.fisk.dataservice.vo.atvserviceanalyse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API昨天和今天调用情况VO
 * @date 2023/4/20 17:19
 */
@Data
public class AtvYasCallApiAnalyseVO {
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
     * 调用次数
     */
    @ApiModelProperty(value = "调用次数")
    public Integer totalCount;
}
