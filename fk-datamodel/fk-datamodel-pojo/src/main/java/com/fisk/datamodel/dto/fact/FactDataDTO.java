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
    public String factTabName;
    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    public int isPublish;
    /**
     * 事实表下字段列表
     */
    public List<FactAttributeDataDTO> attributeList;
    /**
     * 事实表下指标列表
     */
    public List<IndicatorsDataDTO> indicatorsList;
}
