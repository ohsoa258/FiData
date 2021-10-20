package com.fisk.datamodel.dto.fact;

import com.fisk.datamodel.dto.atomicindicator.IndicatorsDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactDataDTO {

    public long id;
    /**
     * 事实表英文名称
     */
    public String factTableEnName;
    /**
     * 事实表下字段列表
     */
    public List<FactAttributeDataDTO> attributeList;
    /**
     * 事实表下指标列表
     */
    public List<IndicatorsDataDTO> indicatorsList;
}
