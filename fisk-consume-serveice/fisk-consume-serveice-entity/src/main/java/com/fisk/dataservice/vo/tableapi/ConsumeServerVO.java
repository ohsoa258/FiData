package com.fisk.dataservice.vo.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-10-07
 * @Description:
 */
@Data
public class ConsumeServerVO {
    @ApiModelProperty(value = "数据消费总数")
    private int totalNumber;
    @ApiModelProperty(value = "数据消费次数")
    private int frequency;
    @ApiModelProperty(value = "数据消费接口数量")
    private int apiNumber;
    @ApiModelProperty(value = "重点数据消费接口数量")
    private int focusApiTotalNumber;
    @ApiModelProperty(value = "数据消费接口成功数")
    private int successNumber;
    @ApiModelProperty(value = "数据消费接口失败数")
    private int faildNumber;
}
