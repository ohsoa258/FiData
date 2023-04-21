package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorsDTO {
    @ApiModelProperty(value = "id")
    public int id;
    @ApiModelProperty(value = "业务域id",required = true)
    public int businessId;
    @ApiModelProperty(value = "事实表id",required = true)
    public int factId;
    @ApiModelProperty(value = "事实字段表id",required = true)
    public int factAttributeId;
    @ApiModelProperty(value = "指示器类型")
    public int indicatorsType;
    @ApiModelProperty(value = "计算逻辑")
    public String calculationLogic;
    @ApiModelProperty(value = "指示器中文名称")
    public String indicatorsCnName;
    @ApiModelProperty(value = "指示器名称")
    public String indicatorsName;
    @ApiModelProperty(value = "指示器详细信息")
    public String indicatorsDes;
    @ApiModelProperty(value = "业务限制id")
    public int businessLimitedId;
    @ApiModelProperty(value = "原子id")
    public int atomicId;
    /**
     * 时间周期
     */
    @ApiModelProperty(value = "时间周期")
    public String timePeriod;
    /**
     * 派生指标类型：0基于原子指标、1基于指标公式
     */
    @ApiModelProperty(value = "派生指标类型：0基于原子指标、1基于指标公式")
    public int derivedIndicatorsType;
    /**
     * 指标公式
     */
    @ApiModelProperty(value = "指标公式")
    public String indicatorsFormula;
}
