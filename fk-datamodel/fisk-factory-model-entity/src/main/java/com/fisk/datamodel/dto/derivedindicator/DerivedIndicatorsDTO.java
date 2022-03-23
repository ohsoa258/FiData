package com.fisk.datamodel.dto.derivedindicator;

import com.fisk.datamodel.dto.derivedindicatorslimited.DerivedIndicatorsLimitedDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsDTO {
    public Long id;
    public String indicatorsName;
    public String indicatorsDes;
    public Long atomicId;
    public Long factId;
    public String timePeriod;
    public int businessId;
    /**
     * 指标类型：0原子指标、1派生指标
     */
    public int indicatorsType;
    /**
     * 派生指标类型：0基于原子公式、1指标公式
     */
    public int derivedIndicatorsType;
    /**
     * 指标公式
     */
    public String indicatorsFormula;
    /**
     * 事实字段表id集合
     */
    public List<Integer> attributeId;
    /**
     * 聚合字段id集合
     */
    public String aggregatedFields;
    /**
     * 业务限定集合
     */
    public List<DerivedIndicatorsLimitedDTO> limitedList;

}
