package com.fisk.common.service.accessAndTask.factorycodepreviewdto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lishiji
 * 并非用于前端传参，后端自行使用，用于整合参数
 */
@Data
public class PreviewTableBusinessDTO {

    @ApiModelProperty(value = "业务域覆盖id")
    public Long id;

    @ApiModelProperty(value = "物理表id", required = true)
    public Long accessId;

    @ApiModelProperty(value = "模式(1:普通模式  2:高级模式)", required = true)
    public Integer otherLogic;

    @ApiModelProperty(value = "1:每年  2:每月  3:每天(传汉字)", required = true)
    public String businessTimeFlag;

    @ApiModelProperty(value = "具体日期", required = true)
    public Long businessDate;

    @ApiModelProperty(value = "业务时间覆盖字段", required = true)
    public String businessTimeField;

    @ApiModelProperty(value = ">,>=,=,<=,<(传符号)", required = true)
    public String businessOperator;

    @ApiModelProperty(value = "业务覆盖范围", required = true)
    public Long businessRange;

    @ApiModelProperty(value = "业务覆盖单位,Year,Month,Day,Hour", required = true)
    public String rangeDateUnit;

    @ApiModelProperty(value = "其他逻辑  >,>=,=,<=,<(传符号)预备值", required = true)
    public String businessOperatorStandby;

    @ApiModelProperty(value = "其他逻辑  业务覆盖范围预备值", required = true)
    public Long businessRangeStandby;

    @ApiModelProperty(value = "其他逻辑  业务覆盖单位,Year,Month,Day,Hour预备值", required = true)
    public String rangeDateUnitStandby;

    @ApiModelProperty(value = "自定义覆盖时间", required = true)
    public String customCoverageTime;

}
