package com.fisk.datamodel.dto.derivedindicator;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsDTO {
    public Long id;
    public String derivedName;
    public String derivedDes;
    public Long atomicId;
    public Long businessLimitedId;
    public Long factId;
    public String timePeriod;
    /**
     * 事实字段表id集合
     */
    public List<Integer> ids;

}
