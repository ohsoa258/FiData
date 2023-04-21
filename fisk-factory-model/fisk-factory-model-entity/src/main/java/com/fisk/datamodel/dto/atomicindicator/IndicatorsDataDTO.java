package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class IndicatorsDataDTO {
    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 事实表id
     */
    @ApiModelProperty(value = "事实表id")
    public int factId;
    /**
     * 指标名称
     */
    @ApiModelProperty(value = "指标名称")
    public String indicatorsName;
    /**
     * 指标类型：0原子指标、1派生指标
     */
    @ApiModelProperty(value = "指标类型：0原子指标、1派生指标")
    public int indicatorsType;
    /**
     * 时间周期：MTD...
     */
    @ApiModelProperty(value = "时间周期：MTD...")
    public String timePeriod;

}
