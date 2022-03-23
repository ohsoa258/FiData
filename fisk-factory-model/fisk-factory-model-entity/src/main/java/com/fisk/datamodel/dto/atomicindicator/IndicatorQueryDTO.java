package com.fisk.datamodel.dto.atomicindicator;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class IndicatorQueryDTO {
    public int businessAreaId;
    public String remark;
    public List<Integer> factIds;
    public List<Integer> wideTableIds;
}
