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

    /**
     * tb_table_access(id)
     */
    @ApiModelProperty(value = "物理表id",required = true)
    public Long accessId;

    /**
     * 开关(0:false  1:true)
     */
    @ApiModelProperty(value = "开关(0:flase  1:true)",required = true)
    public Boolean otherLogic;

    /**
     * 1:每年  2:每月  3:每天
     */
    @ApiModelProperty(value = "1:每年  2:每月  3:每天",required = true)
    public long businessFlag;

    /**
     * 具体日期
     */
    @ApiModelProperty(value = "具体日期",required = true)
    public long businessDate;

    /**
     * 业务时间覆盖字段
     */
    @ApiModelProperty(value = "业务时间覆盖字段",required = true)
    public String businessTimeField;

    /**
     * 1:大于  2:小于  3:等于  4:大于等于  5:小于等于
     */
    @ApiModelProperty(value = "1:大于  2:小于  3:等于  4:大于等于  5:小于等于",required = true)
    public long businessOperator;

    /**
     * 业务覆盖范围
     */
    @ApiModelProperty(value = "业务覆盖范围",required = true)
    public long businessExtent;

    /**
     * 业务覆盖单位
     */
    @ApiModelProperty(value = "业务覆盖单位",required = true)
    public long extentDateUnit;

    /**
     * 其他逻辑  1:大于  2:小于  3:等于  4:大于等于  5:小于等于
     */
    @ApiModelProperty(value = "其他逻辑  1:大于  2:小于  3:等于  4:大于等于  5:小于等于",required = true)
    public long businessOperatorStandby;

    /**
     * 其他逻辑  业务覆盖范围
     */
    @ApiModelProperty(value = "其他逻辑  业务覆盖范围",required = true)
    public long businessExtentStandby;

    /**
     * 其他逻辑  业务覆盖单位
     */
    @ApiModelProperty(value = "其他逻辑  业务覆盖单位",required = true)
    public long extentDateUnitStandby;
}
