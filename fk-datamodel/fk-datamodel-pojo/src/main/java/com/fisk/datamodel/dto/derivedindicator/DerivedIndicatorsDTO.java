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
    public String derivedName;
    public String derivedDes;
    public Long atomicId;
    public Long factId;
    public String timePeriod;
    /**
     * 事实字段表id集合
     */
    public List<Integer> attributeId;
    /**
     * 业务限定集合
     */
    public List<DerivedIndicatorsLimitedDTO> limitedList;

}
