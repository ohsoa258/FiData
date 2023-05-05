package com.fisk.datamodel.dto.derivedindicator;

import com.fisk.datamodel.dto.derivedindicatorslimited.DerivedIndicatorsLimitedDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsDTO {
    @ApiModelProperty(value = "id")
    public Long id;
    @ApiModelProperty(value = "指示器名称")
    public String indicatorsName;
    @ApiModelProperty(value = "指示器详细信息")
    public String indicatorsDes;
    @ApiModelProperty(value = "原子Id")
    public Long atomicId;
    @ApiModelProperty(value = "事实Id")
    public Long factId;
    @ApiModelProperty(value = "时间周期")
    public String timePeriod;
    @ApiModelProperty(value = "业务Id")
    public int businessId;

    /**
     * 指标类型：0原子指标、1派生指标
     */
    @ApiModelProperty(value = "指示器类型 指标类型：0原子指标、1派生指标")
    public int indicatorsType;
    /**
     * 派生指标类型：0基于原子公式、1指标公式
     */
    @ApiModelProperty(value = "指示器驱动类型 派生指标类型：0基于原子公式、1指标公式")
    public int derivedIndicatorsType;
    /**
     * 指标公式
     */
    @ApiModelProperty(value = "指标公式")
    public String indicatorsFormula;
    /**
     * 事实字段表id集合
     */
    @ApiModelProperty(value = "事实字段表id集合")
    public List<Integer> attributeId;
    /**
     * 聚合字段id集合
     */
    @ApiModelProperty(value = "聚合字段id集合")
    public String aggregatedFields;
    /**
     * 业务限定集合
     */
    @ApiModelProperty(value = "业务限定集合")
    public List<DerivedIndicatorsLimitedDTO> limitedList;

}
