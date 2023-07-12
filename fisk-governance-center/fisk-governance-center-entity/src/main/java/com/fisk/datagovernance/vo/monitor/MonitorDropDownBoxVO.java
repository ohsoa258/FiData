package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-07-10
 * @Description:
 */
@Data
public class MonitorDropDownBoxVO {

    @ApiModelProperty(value = "下拉框名称")
    private String name;

    @ApiModelProperty(value = "值")
    private Integer value;

    @ApiModelProperty(value = "单位:1分 2小时 3天")
    private Integer unit;
}
