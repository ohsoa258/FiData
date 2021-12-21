package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorsDTO {
    public int id;
    @ApiModelProperty(value = "业务域id",required = true)
    public int businessId;
    @ApiModelProperty(value = "事实表id",required = true)
    public int factId;
    @ApiModelProperty(value = "事实字段表id",required = true)
    public int factAttributeId;
    public int indicatorsType;
    public String calculationLogic;
    public String indicatorsCnName;
    public String indicatorsName;
    public String indicatorsDes;
    public int businessLimitedId;
    public int atomicId;
    /**
     * 时间周期
     */
    public String timePeriod;
    /**
     * 派生指标类型：0基于原子指标、1基于指标公式
     */
    public int derivedIndicatorsType;
    /**
     * 指标公式
     */
    public String indicatorsFormula;
}
