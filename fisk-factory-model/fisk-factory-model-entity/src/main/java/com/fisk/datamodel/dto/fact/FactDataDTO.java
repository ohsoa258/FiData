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
     * 事实表显示名称
     */
    public String factTableCnName;
    /**
     * 发布状态：0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public int isPublish;
    /**
     * Doris发布状态 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public int dorisPublish;
    /**
     * 事实表下字段列表
     */
    public List<FactAttributeDataDTO> attributeList;
    /**
     * 事实表下指标列表
     */
    public List<IndicatorsDataDTO> indicatorsList;
}
