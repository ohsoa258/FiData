package com.fisk.dataaccess.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableBusinessDTO {

    @ApiModelProperty(value = "业务域覆盖id")
    public long id;

    @ApiModelProperty(value = "物理表id",required = true)
    public Long accessId;

    @ApiModelProperty(value = "模式(1:普通模式  2:高级模式)",required = true)
    public Integer otherLogic;

    @ApiModelProperty(value = "1:每年  2:每月  3:每天",required = true)
    public long businessTimeFlag;

    @ApiModelProperty(value = "具体日期",required = true)
    public long businessDate;

    @ApiModelProperty(value = "业务时间覆盖字段",required = true)
    public String businessTimeField;

    @ApiModelProperty(value = "1:大于  2:小于  3:等于  4:大于等于  5:小于等于",required = true)
    public long businessOperator;

    @ApiModelProperty(value = "业务覆盖范围",required = true)
    public long businessRange;

    @ApiModelProperty(value = "业务覆盖单位,Year,Month,Day,Hour",required = true)
    public String rangeDateUnit;

    @ApiModelProperty(value = "其他逻辑  1:大于  2:小于  3:等于  4:大于等于  5:小于等于",required = true)
    public long businessOperatorStandby;

    @ApiModelProperty(value = "其他逻辑  业务覆盖范围",required = true)
    public long businessRangeStandby;

    @ApiModelProperty(value = "其他逻辑  业务覆盖单位,Year,Month,Day,Hour",required = true)
    public String rangeDateUnitStandby;
}
