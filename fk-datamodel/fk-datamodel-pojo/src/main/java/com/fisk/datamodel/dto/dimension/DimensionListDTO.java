package com.fisk.datamodel.dto.dimension;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionListDTO {

    public long id;
    /**
     * 维度中文名称
     */
    public String dimensionCnName;
    /**
     * 维度应用表名称
     */
    public String dimensionTabName;
    /**
     * 发布状态：0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public int isPublish;
    /**
     * 维度字段列表
     */
    public List<DimensionAttributeDataDTO> attributeList;

}
